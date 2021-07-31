package com.op.ludo.integrationTest.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.op.ludo.controllers.dto.websocket.ActionsWithBoardState;
import com.op.ludo.integrationTest.BoardStompClients;
import com.op.ludo.model.BoardState;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.apache.commons.codec.Charsets;

public class DataReader {

    private static ObjectMapper mapper = getMapper();

    public static <T> T getResource(String path, JavaType type) {
        File file = new File(path);
        T model = null;
        FileReader reader;
        try {
            reader = new FileReader(file);
            model = mapper.readValue(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    public static <T> T getResource(String path, TypeReference<T> type) {
        return getResource(path, mapper.getTypeFactory().constructType(type));
    }

    public static <T> T getResource(String path, Class<T> type) {
        return getResource(path, mapper.getTypeFactory().constructType(type));
    }

    private static ObjectMapper getMapper() {
        if (mapper != null) return mapper;
        return AppObjectMapper.objectMapper();
    }

    public static String getFileAsString(String path) {
        File file = new File(path);
        String event = "";
        try {
            event = Files.asCharSource(file, Charsets.US_ASCII).read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return event;
    }

    public static BoardStompClients.UserCredentials getCredentials1() {
        return getResource(
                "src/integrationTest/resources/data/firebase/Credentials1.json",
                BoardStompClients.UserCredentials.class);
    }

    public static List<BoardStompClients.UserCredentials> getCredentialsList() {
        return getResource(
                "src/integrationTest/resources/data/firebase/AllCredentials.json",
                new TypeReference<>() {});
    }

    public static String getFirebaseAPIKey() {
        return getFileAsString("src/integrationTest/resources/data/firebase/FirebaseAPIKey.txt");
    }

    public static BoardState getReadyToStartBoard() {
        return getResource(
                "src/integrationTest/resources/data/game/BoardReadyState.json", BoardState.class);
    }

    public static ActionsWithBoardState getStartedAction() {
        return getResource(
                "src/integrationTest/resources/data/game/StartedAction.json",
                ActionsWithBoardState.class);
    }

    public static BoardState getStartedBoard() {
        return getResource(
                "src/integrationTest/resources/data/game/BoardStartedState.json", BoardState.class);
    }
}
