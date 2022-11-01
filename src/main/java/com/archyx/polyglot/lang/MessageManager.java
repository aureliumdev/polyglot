package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
import com.archyx.polyglot.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessageManager implements Listener {

    private final Polyglot polyglot;
    private final Map<Locale, LangMessages> langMessagesMap;
    private LangMessages embeddedMessages;
    private final Locale defaultLanguage;
    private final String defaultLanguageCode;

    public MessageManager(Polyglot polyglot) {
        this.polyglot = polyglot;
        this.defaultLanguageCode = polyglot.getConfig().getDefaultLanguage();
        this.defaultLanguage = new Locale(defaultLanguageCode);
        this.langMessagesMap = new HashMap<>();
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

    private String processMessage(String input) {
        MiniMessage mm = MiniMessage.miniMessage();
        Component component = mm.deserialize(input);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    private void generateMessageFiles() {
        String fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        polyglot.getPlugin().saveResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName, false);
    }

    public Locale getDefaultLanguage() {
        return defaultLanguage;
    }

}
