package org.kopingenieria.domain.dto;

import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;

public record ConnectionDTO(String endpointUrl, String applicationName,
                            String applicationUri, String productUri, ConnectionType type, Timeouts timeout) {
}
