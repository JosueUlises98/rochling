package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class DefaultConnectionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 10L;

    private String endpointUrl;
    private String applicationName;
    private String applicationUri;
    private String productUri;
    private ConnectionType type;
    private Timeouts timeout;
}
