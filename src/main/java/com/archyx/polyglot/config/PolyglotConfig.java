package com.archyx.polyglot.config;

import java.util.List;

public class PolyglotConfig {

    private final String defaultLanguage;
    private final String[] providedLanguages;
    private final String messageDirectory;
    private final String messageFileName;
    private final MessageReplacements messageReplacements;
    private final List<String> processExcluded;

    public PolyglotConfig(String defaultLanguage, String[] providedLanguages, String messageDirectory, String messageFileName, MessageReplacements messageReplacements, List<String> processExcluded) {
        this.defaultLanguage = defaultLanguage;
        this.providedLanguages = providedLanguages;
        this.messageDirectory = messageDirectory;
        this.messageFileName = messageFileName;
        this.messageReplacements = messageReplacements;
        this.processExcluded = processExcluded;
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

    public List<String> getProcessExcluded() {
        return processExcluded;
    }
}
