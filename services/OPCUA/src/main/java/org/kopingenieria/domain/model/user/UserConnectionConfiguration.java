package org.kopingenieria.domain.model.user;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class UserConnectionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String endpointUrl;
    private String applicationName;
    private String applicationUri;
    private String productUri;
    private ConnectionType type;
    private Timeouts timeout;
}
