package com.archyx.polyglot.config;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PolyglotConfigBuilder {

    private String defaultLanguage;
    private String messageDirectory;
    private String messageFileName;
    private MessageReplacements messageReplacements;
    private List<String> processExcluded;

    public PolyglotConfigBuilder() {
        this.defaultLanguage = "en";
        this.messageDirectory = "messages";
        this.messageFileName = "messages_{language}.yml";
        this.messageReplacements = new MessageReplacements(new HashMap<>());
        this.processExcluded = new ArrayList<>();
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

    public PolyglotConfigBuilder processExcluded(@NotNull List<String> processExcluded) {
        this.processExcluded = processExcluded;
        return this;
    }

    public PolyglotConfig build() {
        return new PolyglotConfig(defaultLanguage, messageDirectory, messageFileName, messageReplacements, processExcluded);
    }

}
