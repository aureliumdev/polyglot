package com.archyx.polyglot.config;

public class PolyglotConfig {

    private final String defaultLanguage;
    private final String messageDirectory;
    private final String messageFileName;

    public PolyglotConfig(String defaultLanguage, String messageDirectory, String messageFileName) {
        this.defaultLanguage = defaultLanguage;
        this.messageDirectory = messageDirectory;
        this.messageFileName = messageFileName;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public String getMessageDirectory() {
        return messageDirectory;
    }

    public String getMessageFileName() {
        return messageFileName;
    }

}
