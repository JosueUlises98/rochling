package org.kopingenieria.application.service.configuration.user;

import org.kopingenieria.api.response.OpcUaConfigResponse;
import org.kopingenieria.domain.model.user.UserConfigurationOpcUa;

public interface UserConfiguration {

    OpcUaConfigResponse createUserOpcUaClient(UserConfigurationOpcUa useropcua);

}
