package org.kopingenieria.logging.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kopingenieria.logging.model.LogEvent;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(LogEvent object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "Error convirtiendo a JSON: " + e.getMessage(); // Manejo básico de errores
        }
    }
}
