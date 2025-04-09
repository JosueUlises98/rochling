package org.kopingenieria.application.service.configuration.bydefault;

import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.response.configuration.ConfigResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultConfigService {

    private final DefaultConfigComp configuracionPredeterminada;

    public ConfigResponse crearConfiguracionPredeterminada() {
        return configuracionPredeterminada.createDefaultOpcuaC();
    }

    public ConfigResponse obtenerConfiguracionPredeterminada(String clienteId) {
        return configuracionPredeterminada.readDefaultConfiguration(clienteId);
    }

    public ConfigResponse eliminarConfiguracionPredeterminada(String clienteId) {
        return configuracionPredeterminada.deleteDefaultConfiguration(clienteId);
    }

    public List<ConfigResponse> obtenerTodasLasConfiguraciones() {
        return configuracionPredeterminada.getAllConfigurations();
    }
}
