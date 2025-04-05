package org.kopingenieria.api.request.security.bydefault;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.model.user.UserOpcUa;

@Data
@Builder
public class OpcUaAuthenticationRequest {

    private final UserOpcUa userConfig;

}
