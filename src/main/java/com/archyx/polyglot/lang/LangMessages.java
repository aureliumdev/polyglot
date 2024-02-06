package com.archyx.polyglot.lang;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public class LangMessages {

    private final Locale locale;
    private final String languageCode;
    private final Map<MessageKey, String> messages;

    public LangMessages(Locale locale, String languageCode, Map<MessageKey, String> messages) {
        this.locale = locale;
        this.languageCode = languageCode;
        this.messages = messages;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public Map<MessageKey, String> getMessages() {
        return messages;
    }

    @Nullable
    public String getMessage(MessageKey key) {
        return messages.get(key);
    }
}
