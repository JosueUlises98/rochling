package org.kopingenieria.application.service.configuration.bydefault;

import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.request.configuration.DefaultConfigRequest;
import org.kopingenieria.api.response.configuration.bydefault.DefaultConfigResponse;
import org.kopingenieria.application.service.configuration.bydefault.component.DefaultConfigComp;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultConfigService implements DefaultConfig {

    private final DefaultConfigComp configuracionPredeterminada;

    public DefaultConfigResponse crearConfiguracionPredeterminada() {
        return configuracionPredeterminada.createDefaultOpcuaC();
    }

    public DefaultConfigResponse obtenerConfiguracionPredeterminada(DefaultConfigRequest configuracion) {
        return configuracionPredeterminada.readDefaultConfiguration(configuracion.getDefaultConfig().getId());
    }

    public DefaultConfigResponse eliminarConfiguracionPredeterminada(DefaultConfigRequest configuracion) {
        return configuracionPredeterminada.deleteDefaultConfiguration(configuracion.getDefaultConfig().getId());
    }

    public List<DefaultConfigResponse> obtenerTodasLasConfiguraciones() {
        return configuracionPredeterminada.getAllConfigurations();
    }
}
