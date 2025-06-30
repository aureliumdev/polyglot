package com.archyx.polyglot;

import com.archyx.polyglot.config.PolyglotConfigBuilder;

import java.util.function.Consumer;

public class MockPolyglot {

    public static Polyglot mock() {
        return new Polyglot(new PolyglotProviderMock(), new PolyglotConfigBuilder().build());
    }

    public static Polyglot mock(Consumer<PolyglotConfigBuilder> builderConsumer) {
        PolyglotConfigBuilder configBuilder = new PolyglotConfigBuilder();
        builderConsumer.accept(configBuilder);
        return new Polyglot(new PolyglotProviderMock(), configBuilder.build());
    }

}
