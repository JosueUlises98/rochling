package org.kopingenieria.api.response.configuration.bydefault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultConfigResponse {

    // Indica si la configuración fue exitosa
    private boolean exitoso;

    //Cliente opcua de eclipse milo
    private OpcUaClient miloClient;

    //Cliente opcua del dominio
    private DefaultOpcUa client;

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
