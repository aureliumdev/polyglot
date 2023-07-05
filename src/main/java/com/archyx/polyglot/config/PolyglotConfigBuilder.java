package com.archyx.polyglot.config;

import java.util.HashMap;

public class PolyglotConfigBuilder {

    private String defaultLanguage;
    private String messageDirectory;
    private String messageFileName;
    private MessageReplacements messageReplacements;

    public PolyglotConfigBuilder() {
        this.defaultLanguage = "en";
        this.messageDirectory = "messages";
        this.messageFileName = "messages_{language}.yml";
        this.messageReplacements = new MessageReplacements(new HashMap<>());
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

    public PolyglotConfigBuilder messageReplacements(MessageReplacements messageReplacements) {
        this.messageReplacements = messageReplacements;
        return this;
    }

    public PolyglotConfig build() {
        return new PolyglotConfig(defaultLanguage, messageDirectory, messageFileName, messageReplacements);
    }

}
