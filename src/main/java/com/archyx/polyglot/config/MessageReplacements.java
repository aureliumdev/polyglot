package com.archyx.polyglot.config;

import java.util.Map;

public class MessageReplacements {

    private final Map<String, String> replacements;

    public MessageReplacements(Map<String, String> replacements) {
        this.replacements = replacements;
    }

    public Map<String, String> getReplacements() {
        return replacements;
    }

}
