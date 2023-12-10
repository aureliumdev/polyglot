package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
import com.archyx.polyglot.util.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MessageManager {

    private final Polyglot polyglot;
    private final MessageLoader messageLoader;
    private final FileUpdater fileUpdater;
    private final Map<Locale, LangMessages> langMessagesMap;
    private LangMessages embeddedMessages;
    private final Locale defaultLanguage;
    private final String defaultLanguageCode;
    private final List<MessageUpdate> messageUpdates;


    public MessageManager(Polyglot polyglot) {
        this.polyglot = polyglot;
        this.messageLoader = new MessageLoader(polyglot);
        this.fileUpdater = new FileUpdater(polyglot, messageLoader);
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
        if (langMessages != null && !language.equals(Locale.ROOT)) {
            String message = langMessages.getMessage(messageKey);
            if (message != null) {
                return message;
            }
        }
        return getDefaultMessage(messageKey, language);
    }

    public String getDefaultMessage(MessageKey key, Locale locale) {
        if (defaultLanguage == null) {
            throw new IllegalStateException("Default language has not been set");
        }
        LangMessages langMessages = getLangMessages(defaultLanguage);
        if (langMessages == null) {
            langMessages = embeddedMessages;
        }
        String message = langMessages.getMessage(key);
        if (message != null) {
            return message;
        } else {
            message = embeddedMessages.getMessage(key);
        }
        if (message != null) {
            return message;
        } else {
            LangMessages global = getLangMessages(Locale.ROOT);
            if (global != null) {
                message = replaceMessagePlaceholders(global.getMessage(key), locale);
            }
        }
        return message != null ? message : key.getPath();
    }

    private String replaceMessagePlaceholders(String message, Locale locale) {
        String[] placeholders = TextUtil.substringsBetween(message, "{", "}");
        for (String placeholder : placeholders) {
            // Only replace double curly brace placeholders
            if (!placeholder.startsWith("{") && !placeholder.endsWith("}")) {
                continue;
            }
            String path = TextUtil.replace(placeholder, "{", "", "}", "");
            LangMessages lang = getLangMessages(locale);
            if (lang != null) {
                String replacedMsg = lang.getMessage(MessageKey.of(path));
                message = TextUtil.replace(message, "{" + placeholder + "}", replacedMsg);
            }
        }
        return message;
    }

    public Set<Locale> getLoadedLanguages() {
        return langMessagesMap.keySet().stream()
                .filter(locale -> !locale.equals(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public void registerMessageUpdate(MessageUpdate messageUpdate) {
        messageUpdates.add(messageUpdate);
    }

    public void loadMessages() {
        try {
            this.embeddedMessages = messageLoader.loadEmbeddedMessages(defaultLanguageCode);
        } catch (Exception e) {
            polyglot.getProvider().logSevere("Error loading embedded message file, some messages may be missing!");
            e.printStackTrace();
        }

        File messagesDir = new File(polyglot.getProvider().getDataFolder() + "/" + polyglot.getConfig().getMessageDirectory());
        generateMessageFiles();
        File[] messageFiles = messagesDir.listFiles();
        if (messageFiles == null) return;
        int numLoaded = 0;
        for (File file : messageFiles) {
            if (!file.getName().endsWith(".yml")) continue;

            String fileName;
            if (file.getName().equals("global.yml")) {
                fileName = "global.yml";
            } else {
                String language = file.getName().substring(file.getName().indexOf("_") + 1, file.getName().lastIndexOf("."));
                fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", language);
            }
            attemptFileUpdate(file, fileName); // Update the file if necessary

            try {
                // Load and add messages to map
                LangMessages langMessages = messageLoader.loadMessageFile(file);

                langMessagesMap.put(langMessages.getLocale(), langMessages);
                numLoaded++;
            } catch (Exception e) {
                polyglot.getProvider().logWarn("Error loading message file " + file.getName());
                e.printStackTrace();
            }
        }
        polyglot.getProvider().logInfo("Loaded " + numLoaded + " message files");
    }

    private void attemptFileUpdate(File file, String fileName) {
        fileUpdater.updateFile(file, fileName, messageUpdates);
    }

    private void generateMessageFiles() {
        for (String code : polyglot.getConfig().getProvidedLanguages()) {
            String fileName;
            if (code.equals("global")) {
                fileName = "global.yml";
            } else {
                fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", code);
            }
            File file = new File(polyglot.getProvider().getDataFolder(), polyglot.getConfig().getMessageDirectory() + "/" + fileName);
            if (!file.exists()) {
                polyglot.getProvider().saveResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName, false);
            }
        }
    }

    public Locale getDefaultLanguage() {
        return defaultLanguage;
    }

}
