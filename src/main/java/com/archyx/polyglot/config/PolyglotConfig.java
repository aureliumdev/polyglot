package com.archyx.polyglot.config;

public class PolyglotConfig {

    private final String defaultLanguage;
    private final String[] providedLanguages;
    private final String messageDirectory;
    private final String messageFileName;
    private final MessageReplacements messageReplacements;

    public PolyglotConfig(String defaultLanguage, String[] providedLanguages, String messageDirectory, String messageFileName, MessageReplacements messageReplacements) {
        this.defaultLanguage = defaultLanguage;
        this.providedLanguages = providedLanguages;
        this.messageDirectory = messageDirectory;
        this.messageFileName = messageFileName;
        this.messageReplacements = messageReplacements;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public String[] getProvidedLanguages() {
        return providedLanguages;
    }

    public String getMessageDirectory() {
        return messageDirectory;
    }

    public String getMessageFileName() {
        return messageFileName;
    }

    public MessageReplacements getMessageReplacements() {
        return messageReplacements;
    }

}
