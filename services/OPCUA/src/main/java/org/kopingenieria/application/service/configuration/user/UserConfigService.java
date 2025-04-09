package org.kopingenieria.application.service.configuration.user;

import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.response.configuration.ConfigResponse;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserConfigService {

    private final UserConfigComp userConfigComponent;

    public ConfigResponse crearConfiguracionUsuario(UserConfigRequest configuracion) {
        return userConfigComponent.createUserOpcUaClient(configuracion);
    }

    public ConfigResponse obtenerConfiguracion(String clienteId) {
        return userConfigComponent.getUserConfiguration(clienteId);
    }

    public ConfigResponse eliminarConfiguracion(String clienteId) {
        return userConfigComponent.deleteUserConfiguration(clienteId);
    }

    public List<ConfigResponse> obtenerTodasLasConfiguraciones() {
        return userConfigComponent.getAllUserConfigurations();
    }

}
