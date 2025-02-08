package org.kopingenieria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonSerializator<T> {

    private final ObjectMapper objectMapper;

    public JsonSerializator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new CustomJavaTimeModule());
        // Indentación legible
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Ordenar claves alfabéticamente
        objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    public String serializeToString(T object) {
        if (object == null) {
            throw new IllegalArgumentException("Object to serialize cannot be null");
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to string", e);
        }
    }

    public byte[] serializeToBytes(T object) {
        if (object == null) {
            throw new IllegalArgumentException("Object to serialize cannot be null");
        }
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to bytes", e);
        }
    }

    public T deserializeFromString(String jsonString, Class<T> clazz) {
        if (jsonString == null || jsonString.isBlank()) {
            throw new IllegalArgumentException("JSON string to deserialize cannot be null or empty");
        }
        try {
            return objectMapper.readValue(jsonString, clazz);
        }catch (Exception e) {
            throw new RuntimeException("Failed to deserialize string to object", e);
        }
    }

    public T deserializeFromBytes(byte[] jsonBytes, Class<T> clazz) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            throw new IllegalArgumentException("Byte array to deserialize cannot be null or empty");
        }
        try {
            return objectMapper.readValue(jsonBytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize bytes to object", e);
        }
    }
}
