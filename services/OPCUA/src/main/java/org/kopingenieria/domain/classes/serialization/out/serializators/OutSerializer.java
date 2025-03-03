package org.kopingenieria.domain.classes.serialization.out.serializators;

import org.kopingenieria.exceptions.SerializationException;
import org.kopingenieria.domain.classes.serialization.out.serializables.OutSerializable;

public interface OutSerializer<T extends OutSerializable> {
    byte[] serialize(T object) throws SerializationException;
    T deserialize(byte[] data, Class<T> type) throws SerializationException;
    String getFormat();
    T deserialize(byte[] data) throws SerializationException;
}
