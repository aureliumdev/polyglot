package com.archyx.polyglot;

import com.archyx.polyglot.config.PolyglotConfig;
import org.bukkit.plugin.Plugin;

public class Polyglot {

    private final Plugin plugin;
    private final PolyglotConfig config;

    public Polyglot(Plugin plugin, PolyglotConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public PolyglotConfig getConfig() {
        return config;
    }

}
