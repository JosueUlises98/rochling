package org.kopingenieria.application.service.configuration.bydefault;

import org.kopingenieria.api.response.OpcUaConfigResponse;
import org.kopingenieria.domain.model.bydefault.DefaultConfigurationOpcUa;

public interface DefaultConfiguration {
    OpcUaConfigResponse createDefaultOpcuaC(DefaultConfigurationOpcUa defaultopcua);
}
