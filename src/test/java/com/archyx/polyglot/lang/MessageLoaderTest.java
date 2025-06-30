package com.archyx.polyglot.lang;

import com.archyx.polyglot.MockPolyglot;
import com.archyx.polyglot.Polyglot;
import com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MessageLoaderTest {

    private MessageLoader messageLoader;

    @BeforeEach
    void setUp() {
        Polyglot polyglot = MockPolyglot.mock();
        messageLoader = new MessageLoader(polyglot);
    }

    @Test
    void testLoadMessageFile() throws Exception {
        File globalFile = new File(Resources.getResource("messages/global.yml").getFile());
        Optional<LangMessages> globalMessages = messageLoader.loadMessageFile(globalFile);

        assertNotNull(globalMessages.orElse(null));
        assertEquals("test", globalMessages.get().getMessage(MessageKey.of("some.global.message")));

        File enFile = new File(Resources.getResource("messages/messages_en.yml").getFile());
        Optional<LangMessages> enMessages = messageLoader.loadMessageFile(enFile);

        assertNotNull(enMessages.orElse(null));
        assertEquals("test", enMessages.get().getMessage(MessageKey.of("some.message")));

        File zhCnFile = new File(Resources.getResource("messages/messages_zh-CN.yml").getFile());
        Optional<LangMessages> zhCnMessages = messageLoader.loadMessageFile(zhCnFile);

        assertNotNull(zhCnMessages.orElse(null));
        assertEquals("测试", zhCnMessages.get().getMessage(MessageKey.of("some.message")));
    }

    @Test
    void testGetLocaleFromFile() {
        assertEquals(Locale.ROOT, messageLoader.getLocaleFromFile("global.yml"));
        assertEquals(Locale.ENGLISH, messageLoader.getLocaleFromFile("messages_en.yml"));
        assertEquals(Locale.SIMPLIFIED_CHINESE, messageLoader.getLocaleFromFile("messages_zh-CN.yml"));
        assertNull(messageLoader.getLocaleFromFile("messages_en1.yml"));
    }

}
