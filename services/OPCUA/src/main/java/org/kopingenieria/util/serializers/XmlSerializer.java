package org.kopingenieria.util.serializers;

import org.kopingenieria.domain.serialization.user.UserOpcUaSerializable;
import org.kopingenieria.exception.exceptions.SerializationException;
import org.springframework.stereotype.Component;
import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component(value = "XmlSerializer")
public class XmlSerializer<T extends UserOpcUaSerializable> implements OutSerializer<T> {

    private final JAXBContext jaxbContext;

    public XmlSerializer(Class<T> type) throws SerializationException {
        try {
            this.jaxbContext = JAXBContext.newInstance(type);
        } catch (Exception e) {
            throw new SerializationException("Failed to initialize JAXBContext for type: " + type.getName(), e);
        }
    }

    public byte[] serialize(T object) throws SerializationException {
        if (object == null) {
            throw new SerializationException("The object to be serialized cannot be null.");
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            jaxbContext.createMarshaller().marshal(object, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Error occurred while serializing the object: " + e.getMessage(), e);
        }
    }

    public T deserialize(byte[] data, Class<T> type) throws SerializationException {
        if (data == null || data.length == 0) {
            throw new SerializationException("The data to be deserialized cannot be null or empty.");
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            return type.cast(jaxbContext.createUnmarshaller().unmarshal(inputStream));
        } catch (Exception e) {
            throw new SerializationException("Error occurred while deserializing the data: " + e.getMessage(), e);
        }
    }

    public String getFormat() {
        return "XML";
    }

    @SuppressWarnings("unchecked")
    public T deserialize(byte[] data) throws SerializationException {
        if (data == null || data.length == 0) {
            throw new SerializationException("The data to be deserialized cannot be null or empty.");
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            return (T) jaxbContext.createUnmarshaller().unmarshal(inputStream);
        } catch (Exception e) {
            throw new SerializationException("Error occurred while deserializing the data: " + e.getMessage(), e);
        }
    }
}
