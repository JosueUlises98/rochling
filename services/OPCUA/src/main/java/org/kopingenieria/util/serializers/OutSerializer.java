package org.kopingenieria.util.serializers;

import org.kopingenieria.domain.serialization.out.model.OpcUaSerializable;
import org.kopingenieria.exception.exceptions.SerializationException;

public interface OutSerializer<T extends OpcUaSerializable> {
    byte[] serialize(T object) throws SerializationException;
    T deserialize(byte[] data, Class<T> type) throws SerializationException;
    String getFormat();
    T deserialize(byte[] data) throws SerializationException;
}
