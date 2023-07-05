package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
import com.archyx.polyglot.config.MessageReplacements;
import com.archyx.polyglot.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageLoader {

    private final Polyglot polyglot;
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public MessageLoader(Polyglot polyglot) {
        this.polyglot = polyglot;
    }

    public LangMessages loadMessageFile(File file) throws Exception {
        long start = System.nanoTime();
        Locale locale = getLocaleFromFile(file.getName());
        CommentedConfigurationNode root = loadYamlFile(file);

        LangMessages langMessages = loadFromNode(root, locale);
        long end = System.nanoTime();
        // Print time taken in ms
        polyglot.getProvider().logInfo("Loaded message file in " + (end - start) / 1000000.0 + "ms");
        return langMessages;
    }

    public LangMessages loadEmbeddedMessages(String defaultLanguageCode) throws Exception {
        String fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        InputStream is = polyglot.getProvider().getResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName);
        if (is == null) {
            throw new IllegalStateException("Embedded messages file is missing!");
        }

        CommentedConfigurationNode root = loadYamlFile(is);
        return loadFromNode(root, new Locale(defaultLanguageCode));
    }

    private LangMessages loadFromNode(CommentedConfigurationNode root, Locale locale) {
        Map<MessageKey, String> messageMap = new HashMap<>();

        loadChildrenRec(root, messageMap, 0);

        return new LangMessages(locale, messageMap);
    }

    private void loadChildrenRec(ConfigurationNode node, Map<MessageKey, String> messageMap, int depth) {
        List<ConfigurationNode> nodes = new ArrayList<>(node.childrenMap().values().stream().map(o -> (ConfigurationNode) o).toList());
        // Sort nodes to make replacements load first
        nodes.sort(new NodePrioritySorter(polyglot.getConfig().getMessageReplacements(), depth));

        for (ConfigurationNode child : node.childrenMap().values()) {
            String message = child.getString();
            if (message != null) { // Node is a message
                MessageKey key = MessageKey.of(formatPath(child.path()));
                message = processMessage(message, messageMap); // Apply color and formatting
                messageMap.put(key, message);
            } else { // Node is a section
                loadChildrenRec(child, messageMap, depth + 1);
            }
        }
    }

    private Locale getLocaleFromFile(String fileName) {
        String localeName = fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("."));
        return new Locale(localeName);
    }

    private String formatPath(NodePath path) {
        StringBuilder builder = new StringBuilder();
        path.forEach(o -> {
            if (o instanceof String s) {
                builder.append(s).append(".");
            }
        });
        return builder.substring(0, builder.length() - 1);
    }

    public CommentedConfigurationNode loadYamlFile(File file) throws ConfigurateException {
        Path path = file.toPath();

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(path)
                .build();

        return loader.load();
    }

    public CommentedConfigurationNode loadYamlFile(InputStream is) throws ConfigurateException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .source(() -> new BufferedReader(new InputStreamReader(is)))
                .build();

        return loader.load();
    }

    private String processMessage(String input, Map<MessageKey, String> messageMap) {
        input = applyReplacements(input, messageMap);
        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(input);
        String output = LegacyComponentSerializer.legacySection().serialize(component);
        output = applyColorCodes(output);
        return output;
    }

    private String applyColorCodes(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            char COLOR_CHAR = '§';
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        message = matcher.appendTail(buffer).toString();
        return TextUtil.replace(message, "&", "§");
    }

    private String applyReplacements(String input, Map<MessageKey, String> messageMap) {
        MessageReplacements replacements = polyglot.getConfig().getMessageReplacements();

        for (Map.Entry<String, String> entry : replacements.getReplacements().entrySet()) {
            String toReplace = "{" + entry.getKey() + "}";
            String replacement = messageMap.get(MessageKey.of(entry.getValue()));
            if (replacement != null) {
                input = input.replace(toReplace, replacement);
            }
        }
        return input;
    }

}
