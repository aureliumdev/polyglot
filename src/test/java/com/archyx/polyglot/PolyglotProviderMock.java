package com.archyx.polyglot;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PolyglotProviderMock implements PolyglotProvider {

    private static final Logger logger = LoggerFactory.getLogger(PolyglotProviderMock.class);

    @Override
    public InputStream getResource(String path) {
        try {
            return Resources.getResource(path).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveResource(String path, boolean replace) {

    }

    @Override
    public File getDataFolder() {
        return new File(Resources.getResource("").toString());
    }

    @Override
    public void logInfo(String message) {
        logger.info(message);
    }

    @Override
    public void logWarn(String message) {
        logger.warn(message);
    }

    @Override
    public void logSevere(String message) {
        logger.error(message);
    }
}
