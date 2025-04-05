package org.kopingenieria.api.response.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcUaConfigResponse {

    // Indica si la configuración fue exitosa
    private boolean exitoso;

    //Cliente opcua de eclipse milo
    private OpcUaClient miloClient;

    // Identificador único del cliente configurado
    private String clientId;

    // Detalles del endpoint conectado
    private String endpointUrl;
    private String securityMode;
    private String securityPolicy;

    // Mensaje informativo
    private String mensaje;

    // Detalles en caso de error
    private String error;

}
