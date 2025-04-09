package org.kopingenieria.application.service.configuration.user;

import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.response.configuration.user.UserConfigResponse;
import org.kopingenieria.application.service.configuration.user.component.UserConfigComp;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserConfigService implements UserConfig {

    private final UserConfigComp userConfigComponent;

    public UserConfigResponse crearConfiguracionUsuario(UserConfigRequest configuracion) {
        return userConfigComponent.createUserOpcUaClient(configuracion);
    }

    public UserConfigResponse obtenerConfiguracion(UserConfigRequest configuracion) {
        return userConfigComponent.getUserConfiguration(configuracion.getUserConfig().getId());
    }

    public UserConfigResponse eliminarConfiguracion(UserConfigRequest configuracion) {
        return userConfigComponent.deleteUserConfiguration(configuracion.getUserConfig().getId());
    }

    public List<UserConfigResponse> obtenerTodasLasConfiguraciones() {
        return userConfigComponent.getAllUserConfigurations();
    }
}
