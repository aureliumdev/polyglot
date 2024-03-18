package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
import com.archyx.polyglot.config.MessageReplacements;
import com.archyx.polyglot.util.TextUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;

public class MessageLoader {

    private final Polyglot polyglot;

    public MessageLoader(Polyglot polyglot) {
        this.polyglot = polyglot;
    }

    public LangMessages loadMessageFile(File file) throws Exception {
        Locale locale = getLocaleFromFile(file.getName());
        CommentedConfigurationNode root = loadYamlFile(file);

        return loadFromNode(root, locale, getLanguageCode(file.getName()));
    }

    public LangMessages loadEmbeddedMessages(String defaultLanguageCode) throws Exception {
        String fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        InputStream is = polyglot.getProvider().getResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName);
        if (is == null) {
            throw new IllegalStateException("Embedded messages file is missing!");
        }

        CommentedConfigurationNode root = loadYamlFile(is);
        return loadFromNode(root, new Locale(defaultLanguageCode), defaultLanguageCode);
    }

    private LangMessages loadFromNode(CommentedConfigurationNode root, Locale locale, String languageCode) {
        Map<MessageKey, String> messageMap = new HashMap<>();

        loadChildrenRec(root, messageMap, 0);

        return new LangMessages(locale, languageCode, messageMap);
    }

    private void loadChildrenRec(ConfigurationNode node, Map<MessageKey, String> messageMap, int depth) {
        List<ConfigurationNode> nodes = new ArrayList<>(node.childrenMap().values().stream().map(o -> (ConfigurationNode) o).toList());
        // Sort nodes to make replacements load first
        nodes.sort(new NodePrioritySorter(polyglot.getConfig().getMessageReplacements(), depth));

        for (ConfigurationNode child : nodes) {
            String message = child.getString();
            if (message != null) { // Node is a message
                MessageKey key = MessageKey.of(formatPath(child.path()));
                // Make sure the name of the node key is not excluded from processing
                message = applyReplacements(message, messageMap);
                message = processMessage(message); // Apply color and formatting
                messageMap.put(key, message);
            } else { // Node is a section
                loadChildrenRec(child, messageMap, depth + 1);
            }
        }
    }

    private Locale getLocaleFromFile(String fileName) {
        if (fileName.equals("global.yml")) {
            return Locale.ROOT;
        }
        String localeName = getLanguageCode(fileName);
        return new Locale(localeName);
    }

    private String getLanguageCode(String fileName) {
        return fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("."));
    }

    private String formatPath(NodePath path) {
        StringBuilder builder = new StringBuilder();
        if (path.size() == 0) return "";
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
                .headerMode(HeaderMode.PRESERVE)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .build();

        return loader.load();
    }

    public CommentedConfigurationNode loadYamlFile(InputStream is) throws ConfigurateException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .headerMode(HeaderMode.PRESERVE)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .source(() -> new BufferedReader(new InputStreamReader(is)))
                .build();

        return loader.load();
    }

    private String processMessage(String input) {
        // Replace newlines
        return input.replace("\\n", "\n");
    }

    private String applyReplacements(String input, Map<MessageKey, String> messageMap) {
        MessageReplacements replacements = polyglot.getConfig().getMessageReplacements();

        for (Map.Entry<String, String> entry : replacements.getReplacements().entrySet()) {
            String toReplace = entry.getKey();
            String replacement = messageMap.get(MessageKey.of(entry.getValue()));
            if (replacement != null) {
                input = input.replace(toReplace, replacement);
            }
        }
        return input;
    }

}
