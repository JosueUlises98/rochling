package org.kopingenieria.services;

import org.kopingenieria.exceptions.SerializationException;
import org.kopingenieria.domain.classes.serialization.out.serializables.OutSerializable;
import org.kopingenieria.domain.classes.serialization.out.serializators.JsonSerializer;
import org.springframework.stereotype.Service;

@Service(value ="OutSerializerService")
public class OutSerializerService<T extends OutSerializable> {

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
