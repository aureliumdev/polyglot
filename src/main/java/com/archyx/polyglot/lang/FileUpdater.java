package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
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

    public void updateFile(File file, String fileName, List<MessageUpdate> messageUpdates) {
        try {
            CommentedConfigurationNode userRoot = messageLoader.loadYamlFile(file);
            InputStream embeddedInputStream = polyglot.getProvider().getResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName);

            if (embeddedInputStream == null) {
                throw new IllegalArgumentException("No embedded message file with name " + fileName);
            }
            CommentedConfigurationNode embeddedRoot = messageLoader.loadYamlFile(embeddedInputStream);

            if (userRoot.node("file_version").virtual()) { // If user file has no file_version
                throw new IllegalArgumentException("Message file " + fileName + " is missing a file_version");
            }
            if (embeddedRoot.node("file_version").virtual()) { // If embedded file has no file_version
                throw new IllegalStateException("Embedded message file " + fileName + " is missing a file_version");
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
            applyMessageUpdates(messageUpdates, userRoot, embeddedRoot, userFileVersion, embeddedFileVersion, fileName);

            // Set updated user file_version to embedded
            userRoot.node("file_version").set(embeddedFileVersion);

            // Save updated user file
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(file)
                    .build();
            loader.save(userRoot);

            polyglot.getProvider().logInfo(fileName + " was updated to a new file version, " + keysAdded + " new keys were added.");
        } catch (Exception e) {
            polyglot.getProvider().logWarn("Error updating file " + file.getName());
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

    private void applyMessageUpdates(List<MessageUpdate> messageUpdates, ConfigurationNode userRoot, ConfigurationNode embeddedRoot, int userVersion, int embeddedVersion, String fileName) throws SerializationException {
        for (MessageUpdate update : messageUpdates) {
            if (userVersion < update.getVersion() && embeddedVersion >= update.getVersion()) {
                NodePath path = convertToPath(update.getPath());
                ConfigurationNode userNode = userRoot.node(path);
                ConfigurationNode embeddedNode = embeddedRoot.node(path);

                userNode.set(embeddedNode.raw()); // Set the user node to the value of the embedded node

                if (update.getMessage() != null) {
                    polyglot.getProvider().logWarn(fileName + " was changed: " + update.getMessage());
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
