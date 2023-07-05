package com.archyx.polyglot.lang;

import com.archyx.polyglot.config.MessageReplacements;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Comparator;

public class NodePrioritySorter implements Comparator<ConfigurationNode> {

    private final MessageReplacements replacements;
    private final int depth;

    public NodePrioritySorter(MessageReplacements replacements, int depth) {
        this.replacements = replacements;
        this.depth = depth;
    }

    @Override
    public int compare(ConfigurationNode o1, ConfigurationNode o2) {
        // Check if either node is a replacement
        for (String replaceString : replacements.getReplacements().values()) {
            // Get the part of the replaceString path that is relevant to the current depth
            String[] replacePath = replaceString.split("\\.");
            if (replacePath.length > depth) {
                // Check if either node is a replacement
                if (getKey(o1).equals(replacePath[depth])) {
                    // Return higher priority for o1
                    return -1;
                }
                else if (getKey(o2).equals(replacePath[depth])) {
                    // Return higher priority for o2
                    return 1;
                }
            }
        }
        return 0;
    }

    private String getKey(ConfigurationNode node) {
        Object obj = node.key();
        if (obj instanceof String) {
            return (String) obj;
        } else {
            return "";
        }
    }

}
