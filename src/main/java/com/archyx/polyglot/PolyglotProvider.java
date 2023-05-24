package com.archyx.polyglot;

import java.io.File;
import java.io.InputStream;

public interface PolyglotProvider {

    InputStream getResource(String path);

    void saveResource(String path, boolean replace);

    File getDataFolder();

    void logInfo(String message);

    void logWarn(String message);

    void logSevere(String message);

}
