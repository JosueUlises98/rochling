package org.kopingenieria.domain.serialization.user;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.kopingenieria.domain.model.user.UserAuthenticationConfiguration;
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
@JsonRootName(value = "opcua-authentication-data")
@XmlRootElement(name = "opcua-authentication-data")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"authentication", "timestamp", "metadata"})
public class UserAuthenticationSerializable extends UserOpcUaSerializable {

    @Serial
    private static final long serialVersionUID = 300L;

    @JsonProperty("authentication")
    @XmlElement(name = "authentication")
    @NotNull(message = "La solicitud de autenticacion OPC UA no puede ser nula")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private UserAuthenticationConfiguration authentication;

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

    public UserAuthenticationSerializable(UserAuthenticationSerializable source) {
        this.authentication = source.getAuthentication();
        this.timestamp = source.getTimestamp();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    // Métodos de validación
    @JsonIgnore
    public boolean isValid() {
        return authentication != null && timestamp != null && metadata != null;
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

    public UserAuthenticationSerializable clone() {
        try {
            UserAuthenticationSerializable clone = (UserAuthenticationSerializable) super.clone();
            clone.authentication = this.authentication;
            clone.timestamp = this.timestamp;
            clone.metadata = new HashMap<>(this.metadata);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar OpcUaAuthenticationSerializable", e);
        }
    }

    public String toString() {
        return "OpcUaAuthenticationSerializable{" +
                "authentication=" + authentication +
                ", timestamp=" + timestamp +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }
}
