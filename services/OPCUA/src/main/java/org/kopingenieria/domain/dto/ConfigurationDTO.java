package org.kopingenieria.domain.dto;

import org.kopingenieria.domain.model.*;

public record ConfigurationDTO(ConnectionConfiguration connection,
                               EncryptionConfiguration encryption,
                               IndustrialConfiguration industrial,
                               AuthenticationConfiguration authentication,
                               SubscriptionConfiguration subscription,
                               SessionConfiguration session) {
}
