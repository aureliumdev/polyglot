package com.archyx.polyglot.lang;

import com.archyx.polyglot.Polyglot;
import com.archyx.polyglot.util.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

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
            return messageKey.getPath();
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
        try {
            this.embeddedMessages = messageLoader.loadEmbeddedMessages(defaultLanguageCode);
        } catch (Exception e) {
            polyglot.getProvider().logSevere("Error loading embedded message file, some messages may be missing!");
            e.printStackTrace();
        }

        File messagesDir = new File(polyglot.getProvider().getDataFolder() + "/" + polyglot.getConfig().getMessageDirectory());
        String defaultFileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        File defaultMessage = new File(polyglot.getProvider().getDataFolder() + "/" + polyglot.getConfig().getMessageDirectory() + "/" + defaultFileName);
        if (!messagesDir.exists() || !defaultMessage.exists()) {
            generateMessageFiles();
        }
        File[] messageFiles = messagesDir.listFiles();
        if (messageFiles == null) return;
        int numLoaded = 0;
        for (File file : messageFiles) {
            if (!file.getName().endsWith(".yml")) continue;
            // Update the file if necessary
            String language = file.getName().substring(file.getName().indexOf("_") + 1, file.getName().lastIndexOf("."));
            attemptFileUpdate(file, language);

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

    private void attemptFileUpdate(File file, String language) {
        fileUpdater.updateFile(file, language, messageUpdates);
    }

    private void generateMessageFiles() {
        String fileName = TextUtil.replace(polyglot.getConfig().getMessageFileName(), "{language}", defaultLanguageCode);
        polyglot.getProvider().saveResource(polyglot.getConfig().getMessageDirectory() + "/" + fileName, false);
    }

    public Locale getDefaultLanguage() {
        return defaultLanguage;
    }

}
