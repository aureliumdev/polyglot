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

    public static class PolyglotConfigBuilder {

        private String defaultLanguage;
        private String messageDirectory;
        private String messageFileName;

        public PolyglotConfigBuilder() {
            this.defaultLanguage = "en";
            this.messageDirectory = "messages";
            this.messageFileName = "messages_{language}.yml";
        }

        public void defaultLanguage(String defaultLanguage) {
            this.defaultLanguage = defaultLanguage;
        }

        public void messageDirectory(String messageDirectory) {
            this.messageDirectory = messageDirectory;
        }

        public void messageFileName(String messageFileName) {
            this.messageFileName = messageFileName;
        }

        public PolyglotConfig build() {
            return new PolyglotConfig(defaultLanguage, messageDirectory, messageFileName);
        }

    }

}
