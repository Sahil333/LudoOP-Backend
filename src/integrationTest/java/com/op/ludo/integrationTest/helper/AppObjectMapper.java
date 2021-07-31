package com.op.ludo.integrationTest.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.op.ludo.game.action.AbstractAction;

public class AppObjectMapper {

    private static ObjectMapper objectMapper;

    private AppObjectMapper() {}

    public static ObjectMapper objectMapper() {
        if (objectMapper != null) return objectMapper;
        objectMapper = new ObjectMapper();
        SimpleModule appModule = new SimpleModule();
        appModule.addDeserializer(AbstractAction.class, new AbstractActionDeserializer());
        objectMapper.registerModule(appModule);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
