package org.kopingenieria.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcUaConfigResponse {

    // Indica si la configuración fue exitosa
    private boolean exitoso;

    // Identificador único del cliente configurado
    private String clientId;

    // Detalles del endpoint conectado
    private String endpointUrl;
    private String securityMode;
    private String securityPolicy;

    // Estado de la conexión
    private String estadoConexion;

    // Información de la sesión establecida
    private String sessionId;
    private Long sessionTimeout;

    // Mensaje informativo
    private String mensaje;

    // Detalles en caso de error
    private String error;
}
