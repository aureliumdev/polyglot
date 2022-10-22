package com.archyx.polyglot.config;

public class PolyglotConfigBuilder {

    private String defaultLanguage;
    private String messageDirectory;
    private String messageFileName;

    public PolyglotConfigBuilder() {
        this.defaultLanguage = "en";
        this.messageDirectory = "messages";
        this.messageFileName = "messages_{language}.yml";
    }

    public PolyglotConfigBuilder defaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }

    public PolyglotConfigBuilder messageDirectory(String messageDirectory) {
        this.messageDirectory = messageDirectory;
        return this;
    }

    public PolyglotConfigBuilder messageFileName(String messageFileName) {
        this.messageFileName = messageFileName;
        return this;
    }

    public PolyglotConfig build() {
        return new PolyglotConfig(defaultLanguage, messageDirectory, messageFileName);
    }

}
