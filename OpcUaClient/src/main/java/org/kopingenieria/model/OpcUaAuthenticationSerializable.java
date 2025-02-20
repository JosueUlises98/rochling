package org.kopingenieria.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@JsonPropertyOrder({"authentication-request","authentication-response", "authentication-timestamp","authentication-metadata"})
public class OpcUaAuthenticationSerializable extends OpcUaSerializable {

    @Serial
    private static final long serialVersionUID = 3L;

    @JsonProperty("authentication-request")
    @XmlElement(name = "authentication-request")
    @NotNull(message = "La solicitud de autenticacion OPC UA no puede ser nula")
    private AuthenticationRequest request;

    @JsonProperty("authentication-response")
    @XmlElement(name = "authentication-response")
    @NotNull(message = "La respuesta de autenticacion OPC UA no puede ser nula")
    private AuthenticationResponse response;

    @JsonProperty("authentication-timestamp")
    @XmlElement(name = "authentication-timestamp")
    @NotNull(message = "TimeStamp no puede ser nulo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime timestamp;

    @JsonProperty("authentication-metadata")
    @XmlElement(name = "authentication-metadata")
    @NotNull(message = "Los metadatos no pueden ser nulos")
    private Map<String, String> metadata;

    @JsonIgnore
    private transient volatile Object lockObject;

    public OpcUaAuthenticationSerializable(OpcUaAuthenticationSerializable source) {
        this.request = source.getRequest();
        this.response = source.getResponse();
        this.timestamp = source.getTimestamp();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    // Métodos de validación
    @JsonIgnore
    public boolean isValid() {
        return request != null && response != null && timestamp != null && metadata != null;
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

    public OpcUaAuthenticationSerializable clone() {
        try {
            OpcUaAuthenticationSerializable clone = (OpcUaAuthenticationSerializable) super.clone();
            clone.request = this.request;
            clone.response = this.response;
            clone.timestamp = this.timestamp;
            clone.metadata = new HashMap<>(this.metadata);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar OpcUaAuthenticationSerializable", e);
        }
    }

    public String toString() {
        return "OpcUaAuthenticationSerializable{" +
                "authenticationId='" + (request != null ? request.toString() : "null") + '\'' +
                ", responseId='" + (response != null ? response.toString() : "null") + '\'' +
                ", timestamp=" + timestamp +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }
}
