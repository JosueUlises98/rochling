package org.kopingenieria.domain.classes.serialization.out.serializators;

import org.kopingenieria.domain.classes.serialization.out.serializables.OpcUaSerializable;
import org.kopingenieria.exceptions.SerializationException;

public interface OutSerializer<T extends OpcUaSerializable> {
    byte[] serialize(T object) throws SerializationException;
    T deserialize(byte[] data, Class<T> type) throws SerializationException;
    String getFormat();
    T deserialize(byte[] data) throws SerializationException;
}
