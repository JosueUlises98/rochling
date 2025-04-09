package org.kopingenieria.application.service.configuration.bydefault;

import org.kopingenieria.api.request.configuration.DefaultConfigRequest;
import org.kopingenieria.api.response.configuration.bydefault.DefaultConfigResponse;
import java.util.List;

public interface DefaultConfig {

     DefaultConfigResponse crearConfiguracionPredeterminada();

     DefaultConfigResponse obtenerConfiguracionPredeterminada(DefaultConfigRequest configuracion);

     DefaultConfigResponse eliminarConfiguracionPredeterminada(DefaultConfigRequest configuracion);

     List<DefaultConfigResponse> obtenerTodasLasConfiguraciones();
}
