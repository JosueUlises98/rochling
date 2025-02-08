package org.kopingenieria.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomJavaTimeModule extends SimpleModule {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"; // Formato ISO8601

    public CustomJavaTimeModule() {
        super("CustomJavaTimeModule");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

        // Serializador de LocalDateTime
        addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers)
                    throws IOException {
                gen.writeString(value.format(formatter));
            }
        });

        // Deserializador de LocalDateTime
        addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                return LocalDateTime.parse(p.getText(), formatter);
            }
        });
    }
}
