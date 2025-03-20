package org.kopingenieria.application.service;

import org.kopingenieria.domain.serialization.out.serializables.OpcUaSerializable;
import org.kopingenieria.domain.serialization.out.serializators.JsonSerializer;
import org.kopingenieria.exception.SerializationException;
import org.springframework.stereotype.Service;

@Service(value ="OutSerializerService")
public class OutSerializerService<T extends OpcUaSerializable> {

    private final JsonSerializer<T>jsonSerializer;

    public OutSerializerService(){
        jsonSerializer = new JsonSerializer<>();
    }

    public byte[] serialize(T serializable) throws SerializationException {
        return jsonSerializer.serialize(serializable);
    }

    public T deserialize(byte[] object, Class<T> clazz) throws SerializationException {
        return jsonSerializer.deserialize(object,clazz);
    }

}
