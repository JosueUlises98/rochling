package org.kopingenieria.domain.serialization.out.serializators;

import org.kopingenieria.domain.serialization.out.serializables.OpcUaSerializable;
import org.kopingenieria.exception.SerializationException;

public interface OutSerializer<T extends OpcUaSerializable> {
    byte[] serialize(T object) throws SerializationException;
    T deserialize(byte[] data, Class<T> type) throws SerializationException;
    String getFormat();
    T deserialize(byte[] data) throws SerializationException;
}
