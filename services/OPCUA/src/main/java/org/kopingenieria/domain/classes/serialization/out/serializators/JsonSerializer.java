package org.kopingenieria.domain.classes.serialization.out.serializators;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kopingenieria.exceptions.SerializationException;
import org.kopingenieria.domain.classes.serialization.out.serializables.OutSerializable;
import org.springframework.stereotype.Component;

@Component(value = "JsonSerializer")
public class JsonSerializer<T extends OutSerializable> implements OutSerializer<T> {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] serialize(T object) throws SerializationException {
        if (object == null) {
            throw new SerializationException("The object to be serialized cannot be null.");
        }
        try {
            return mapper.writeValueAsBytes(object);
        } catch (Exception e) {
            throw new SerializationException("Error occurred while serializing the object: " + e.getMessage(), e);
        }
    }

    public String serializeToString(T object) throws SerializationException {
        if (object == null) {
            throw new SerializationException("The object to be serialized cannot be null.");
        }
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new SerializationException("Error occurred while serializing the object to String: " + e.getMessage(), e);
        }
    }

    public T deserializeFromString(String data, Class<T> type) throws SerializationException {
        if (data == null || data.isEmpty()) {
            throw new SerializationException("The data to be deserialized cannot be null or empty.");
        }
        try {
            return mapper.readValue(data, type);
        } catch (Exception e) {
            throw new SerializationException("Error occurred while deserializing the data from String: " + e.getMessage(), e);
        }
    }

    public T deserialize(byte[] data, Class<T> type) throws SerializationException {
        if (data == null) {
            throw new SerializationException("The data to be deserialized cannot be null.");
        }
        try {
            return mapper.readValue(data, type);
        } catch (Exception e) {
            throw new SerializationException("Error occurred while deserializing the data: " + e.getMessage(), e);
        }
    }

    public String getFormat() {
        return "JSON";
    }

    @SuppressWarnings("unchecked")
    public T deserialize(byte[] data) throws SerializationException {
        if (data == null) {
            throw new SerializationException("The data to be deserialized cannot be null.");
        }
        try {
            return (T) mapper.readValue(data, OutSerializable.class);
        } catch (Exception e) {
            throw new SerializationException("Error occurred while deserializing the data: " + e.getMessage(), e);
        }
    }
}
