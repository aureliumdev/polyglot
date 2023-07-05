package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
import com.archyx.polyglot.util.TextUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUpdater {

    private final Polyglot polyglot;
    private final MessageLoader messageLoader;

    public FileUpdater(Polyglot polyglot, MessageLoader messageLoader) {
        this.polyglot = polyglot;
        this.messageLoader = messageLoader;
    }

    public void updateFile(File file, String language, List<MessageUpdate> messageUpdates) {
        try {
            CommentedConfigurationNode userRoot = messageLoader.loadYamlFile(file);
            String defaultFileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", language);
            InputStream embeddedInputStream = polyglot.getProvider().getResource(polyglot.getConfig().getMessageDirectory() + "/" + defaultFileName);

            if (embeddedInputStream == null) {
                throw new IllegalArgumentException("No embedded message file found for language " + language);
            }
            CommentedConfigurationNode embeddedRoot = messageLoader.loadYamlFile(embeddedInputStream);

            if (userRoot.node("file_version").virtual()) { // If user file has no file_version
                throw new IllegalArgumentException("Message file " + file.getName() + " is missing a file_version");
            }
            if (embeddedRoot.node("file_version").virtual()) { // If embedded file has no file_version
                throw new IllegalStateException("Embedded message file for language " + language + " is missing a file_version");
            }

            int userFileVersion = userRoot.node("file_version").getInt();
            int embeddedFileVersion = embeddedRoot.node("file_version").getInt();

            int keysAdded = 0;
            // If user file is up-to-date, return
            if (userFileVersion == embeddedFileVersion) {
                return;
            }

            keysAdded = updateChildren(embeddedRoot, userRoot, keysAdded); // Recursively update messages

            // Apply message update overrides
            applyMessageUpdates(messageUpdates, userRoot, embeddedRoot, userFileVersion, embeddedFileVersion, language);

            // Set updated user file_version to embedded
            userRoot.node("file_version").set(embeddedFileVersion);

            // Save updated user file
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(file)
                    .build();
            loader.save(userRoot);

            polyglot.getProvider().logInfo("messages_" + language + ".yml was updated to a new file version, " + keysAdded + " new keys were added.");
        } catch (Exception e) {
            polyglot.getProvider().logWarn("Error updating file " + file.getName() + " for language " + language);
            e.printStackTrace();
        }
    }

    private int updateChildren(ConfigurationNode embedded, ConfigurationNode userRoot, int keysAdded) throws SerializationException {
        for (ConfigurationNode child : embedded.childrenMap().values()) {
            // Skip file_version node
            if ("file_version".equals(child.key())) {
                continue;
            }
            String message = child.getString();
            if (message != null) { // Node is a message
                if (userRoot.node(child.path()).virtual()) { // User file does not have the embedded path
                    userRoot.node(child.path()).set(message); // Add embedded message to user file
                    keysAdded++;
                }
            } else { // Node is a section
                return updateChildren(child, userRoot, keysAdded);
            }
        }
        return keysAdded;
    }

    private void applyMessageUpdates(List<MessageUpdate> messageUpdates, ConfigurationNode userRoot, ConfigurationNode embeddedRoot, int userVersion, int embeddedVersion, String language) throws SerializationException {
        for (MessageUpdate update : messageUpdates) {
            if (userVersion < update.getVersion() && embeddedVersion >= update.getVersion()) {
                NodePath path = convertToPath(update.getPath());
                ConfigurationNode userNode = userRoot.node(path);
                ConfigurationNode embeddedNode = embeddedRoot.node(path);

                userNode.set(embeddedNode.raw()); // Set the user node to the value of the embedded node

                if (update.getMessage() != null) {
                    polyglot.getProvider().logWarn("messages_" + language + ".yml was changed: " + update.getMessage());
                }
            }
        }
    }

    private NodePath convertToPath(String stringPath) {
        String[] split = stringPath.split("\\.");
        List<String> elements = new ArrayList<>();
        Collections.addAll(elements, split);
        return NodePath.of(elements);
    }

}
