package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
import com.archyx.polyglot.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager implements Listener {

    private final Polyglot polyglot;
    private final Map<Locale, LangMessages> langMessagesMap;
    private LangMessages embeddedMessages;
    private final Locale defaultLanguage;
    private final String defaultLanguageCode;
    private final List<MessageUpdate> messageUpdates;
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public MessageManager(Polyglot polyglot) {
        this.polyglot = polyglot;
        this.defaultLanguageCode = polyglot.getConfig().getDefaultLanguage();
        this.defaultLanguage = new Locale(defaultLanguageCode);
        this.langMessagesMap = new HashMap<>();
        this.messageUpdates = new ArrayList<>();
    }

    @Nullable
    public LangMessages getLangMessages(Locale locale) {
        return langMessagesMap.get(locale);
    }

    public String get(Locale language, MessageKey messageKey) {
        LangMessages langMessages = getLangMessages(language);
        if (langMessages != null) {
            String message = langMessages.getMessage(messageKey);
            if (message != null) {
                return message;
            } else {
                return getDefaultMessage(messageKey);
            }
        } else {
            return getDefaultMessage(messageKey);
        }
    }

    public String getDefaultMessage(MessageKey messageKey) {
        if (defaultLanguage == null) {
            throw new IllegalStateException("Default language has not been set");
        }
        LangMessages langMessages = getLangMessages(defaultLanguage);
        if (langMessages == null) {
            langMessages = embeddedMessages;
        }
        String message = langMessages.getMessage(messageKey);
        if (message != null) {
            return message;
        } else {
            message = embeddedMessages.getMessage(messageKey);
        }
        if (message == null) {
            throw new IllegalArgumentException("Message with message key " + messageKey.getPath() + " not found for default language");
        }
        return message;
    }

    public Set<Locale> getLoadedLanguages() {
        return langMessagesMap.keySet();
    }

    public void registerMessageUpdate(MessageUpdate messageUpdate) {
        messageUpdates.add(messageUpdate);
    }

    public void loadMessages() {
        loadEmbeddedMessages(); // Load embedded messages as backup

        File messagesDir = new File(polyglot.getPlugin().getDataFolder() + "/" + polyglot.getConfig().getMessageDirectory());
        String defaultFileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        File defaultMessage = new File(polyglot.getPlugin().getDataFolder() + "/" + polyglot.getConfig().getMessageDirectory() + "/" + defaultFileName);
        if (!messagesDir.exists() || !defaultMessage.exists()) {
            generateMessageFiles();
        }
        File[] messageFiles = messagesDir.listFiles();
        if (messageFiles == null) return;
        int numLoaded = 0;
        for (File file : messageFiles) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            LangMessages langMessages = loadMessageFile(file.getName(), config);
            if (langMessages != null) {
                langMessagesMap.put(langMessages.getLocale(), langMessages);
                numLoaded++;
            }
        }
        polyglot.getPlugin().getLogger().info("Loaded " + numLoaded + " message files");
    }

    private LangMessages loadMessageFile(String fileName, FileConfiguration config) {
        String localeName = fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("."));
        Locale locale = new Locale(localeName);

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) return null;

        // Add each message key to map
        Map<MessageKey, String> messages = new HashMap<>();
        for (String key : messagesSection.getKeys(true)) {
            if (messagesSection.isConfigurationSection(key)) continue; // Filter out configuration sections
            String message = messagesSection.getString(key);
            String processedMessage = processMessage(message);
            messages.put(MessageKey.of(key), processedMessage);
        }
        // Add messages to language map
        return new LangMessages(locale, messages);
    }

    private void loadEmbeddedMessages() {
        String fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        InputStream is = polyglot.getPlugin().getResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName);
        if (is == null) {
            throw new IllegalStateException("Embedded messages file is missing!");
        }
        InputStreamReader reader = new InputStreamReader(is);
        FileConfiguration config = YamlConfiguration.loadConfiguration(reader);

        LangMessages langMessages = loadMessageFile(fileName, config);
        if (langMessages == null) {
            throw new IllegalStateException("Error loading embedded messages file");
        }

        this.embeddedMessages = langMessages;
    }

    private FileConfiguration updateFile(File file, FileConfiguration config, String language) {
        if (!config.contains("file_version")) {
            return YamlConfiguration.loadConfiguration(file);
        }
        String defaultFileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        InputStream stream = polyglot.getPlugin().getResource(polyglot.getConfig().getMessageDirectory() + "/" + defaultFileName);
        if (stream != null) {
            int currentVersion = config.getInt("file_version");
            FileConfiguration imbConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
            int imbVersion = imbConfig.getInt("file_version");
            //If versions do not match
            if (currentVersion != imbVersion) {
                try {
                    ConfigurationSection configSection = imbConfig.getConfigurationSection("");
                    int keysAdded = 0;
                    if (configSection != null) {
                        for (String key : configSection.getKeys(true)) {
                            if (!configSection.isConfigurationSection(key)) {
                                if (!config.contains(key)) {
                                    config.set(key, imbConfig.get(key));
                                    keysAdded++;
                                }
                            }
                        }
                        // Messages to override
                        for (MessageUpdate update : messageUpdates) {
                            if (currentVersion < update.getVersion() && imbVersion >= update.getVersion()) {
                                ConfigurationSection section = imbConfig.getConfigurationSection(update.getPath());
                                if (section != null) {
                                    for (String key : section.getKeys(false)) {
                                        config.set(section.getCurrentPath() + "." + key, section.getString(key));
                                    }
                                    if (update.getMessage() != null) {
                                        polyglot.getPlugin().getLogger().warning("messages_" + language + ".yml was changed: " + update.getMessage());
                                    }
                                } else {
                                    Object value = imbConfig.get(update.getPath());
                                    if (value != null) {
                                        config.set(update.getPath(), value);
                                        if (update.getMessage() != null) {
                                            polyglot.getPlugin().getLogger().warning("messages_" + language + ".yml was changed: " + update.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    config.set("file_version", imbVersion);
                    config.save(file);
                    polyglot.getPlugin().getLogger().info("messages_" + language + ".yml was updated to a new file version, " + keysAdded + " new keys were added.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private String processMessage(String input) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(input);
        String output = LegacyComponentSerializer.legacySection().serialize(component);
        output = applyColorCodes(output);
        return output;
    }

    private String applyColorCodes(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            char COLOR_CHAR = ChatColor.COLOR_CHAR;
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        message = matcher.appendTail(buffer).toString();
        return TextUtil.replace(message, "&", "§");
    }

    private void generateMessageFiles() {
        String fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        polyglot.getPlugin().saveResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName, false);
    }

    public Locale getDefaultLanguage() {
        return defaultLanguage;
    }

}
