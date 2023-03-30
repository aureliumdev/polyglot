package com.archyx.polyglot.lang;

public interface MessageUpdate {
    int getVersion();

    String getPath();

    String getMessage();
}
