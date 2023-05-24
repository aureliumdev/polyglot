package com.archyx.polyglot;

import com.archyx.polyglot.config.PolyglotConfig;
import com.archyx.polyglot.lang.MessageManager;

public class Polyglot {

    private final PolyglotConfig config;
    private final MessageManager messageManager;
    private final PolyglotProvider provider;

    public Polyglot(PolyglotProvider provider, PolyglotConfig config) {
        this.provider = provider;
        this.config = config;
        this.messageManager = new MessageManager(this);
    }

    public PolyglotProvider getProvider() {
        return provider;
    }

    public PolyglotConfig getConfig() {
        return config;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
