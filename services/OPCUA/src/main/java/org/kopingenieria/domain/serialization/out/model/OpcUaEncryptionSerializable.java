package org.kopingenieria.domain.serialization.out.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.kopingenieria.domain.model.EncryptionConfiguration;
import javax.xml.bind.annotation.*;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonRootName(value = "opcua-encryption-data")
@XmlRootElement(name = "opcua-encryption-data")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"encryption", "timestamp","metadata"})
public class OpcUaEncryptionSerializable extends OpcUaSerializable {

    @Serial
    private static final long serialVersionUID = 400L;

    @JsonProperty("encryption")
    @XmlElement(name = "encryption")
    @NotNull(message = "La encriptacion no puede ser nula o vacia")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EncryptionConfiguration encryption;

    @JsonProperty("timestamp")
    @XmlElement(name = "timestamp")
    @NotNull(message = "TimeStamp no puede ser nulo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime timestamp;

    @JsonProperty("metadata")
    @XmlElement(name = "metadata")
    @NotNull(message = "Los metadatos no pueden ser nulos")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Map<String, String> metadata;

    @JsonIgnore
    @XmlTransient
    private transient volatile Object lockObject;

    public OpcUaEncryptionSerializable(OpcUaEncryptionSerializable source) {
        this.encryption = source.getEncryption();
        this.timestamp = source.getTimestamp();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    // Métodos de validación
    @JsonIgnore
    public boolean isValid() {
        return encryption != null && timestamp != null && metadata != null;
    }

    // Métodos de utilidad para gestionar metadata
    @JsonIgnore
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    @JsonIgnore
    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public OpcUaEncryptionSerializable clone() {
        try {
            OpcUaEncryptionSerializable clone = (OpcUaEncryptionSerializable) super.clone();
            clone.encryption = this.encryption;
            clone.timestamp = this.timestamp;
            clone.metadata = new HashMap<>(this.metadata);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar OpcUaConnectionSerializable", e);
        }
    }

    public String toString() {
        return "OpcUaConnectionSerializable{" +
                ", encryption=" + encryption +
                ", timestamp=" + timestamp +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }
}
