package com.op.ludo.integrationTest.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Files;
import com.op.ludo.model.BoardState;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
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
    mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
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

  public static Map<String, String> getCredentials1() {
    return getResource(
        "src/integrationTest/resources/data/firebase/Credentials1.json", new TypeReference<>() {});
  }

  public static String getFirebaseAPIKey() {
    return getFileAsString("src/integrationTest/resources/data/firebase/FirebaseAPIKey.txt");
  }

  public static BoardState getReadyToStartBoard() {
    return getResource(
        "src/integrationTest/resources/data/game/BoardReadyState.json", BoardState.class);
  }

  public static String getStartedAction() {
    return getFileAsString("src/integrationTest/resources/data/game/StartedAction.txt");
  }
}
