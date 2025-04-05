package org.kopingenieria.application.service.configuration.user;

import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.response.configuration.OpcUaConfigResponse;

public interface UserConfiguration {

    OpcUaConfigResponse createUserOpcUaClient(UserConfigRequest useropcua);

}
