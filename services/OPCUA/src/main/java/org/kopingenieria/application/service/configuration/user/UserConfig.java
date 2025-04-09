package org.kopingenieria.application.service.configuration.user;

import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.response.configuration.user.UserConfigResponse;
import java.util.List;

public interface UserConfig {

    UserConfigResponse crearConfiguracionUsuario(UserConfigRequest configuracion);

    UserConfigResponse obtenerConfiguracion(UserConfigRequest configuracion);

    UserConfigResponse eliminarConfiguracion(UserConfigRequest configuracion);

    List<UserConfigResponse> obtenerTodasLasConfiguraciones();
}
