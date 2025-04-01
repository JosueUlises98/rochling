package org.kopingenieria.util.serializers.user;

import org.kopingenieria.domain.serialization.user.UserOpcUaSerializable;
import org.kopingenieria.exception.exceptions.SerializationException;

public interface OutSerializer<T extends UserOpcUaSerializable> {
    byte[] serialize(T object) throws SerializationException;
    T deserialize(byte[] data, Class<T> type) throws SerializationException;
    String getFormat();
    T deserialize(byte[] data) throws SerializationException;
}
