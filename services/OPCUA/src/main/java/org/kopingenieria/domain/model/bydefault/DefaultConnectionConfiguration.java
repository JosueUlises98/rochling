package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public class DefaultConnectionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String endpointUrl;
    private final String applicationName;
    private final String applicationUri;
    private final String productUri;
    private final ConnectionType type;
    private final Timeouts timeout=Timeouts.CONNECTION;
    @Setter
    private ConnectionStatus status;
}
