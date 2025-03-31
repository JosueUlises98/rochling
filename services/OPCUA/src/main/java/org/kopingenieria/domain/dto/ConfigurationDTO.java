package org.kopingenieria.domain.dto;

import org.kopingenieria.domain.model.user.*;

public record ConfigurationDTO(UserConnectionConfiguration connection,
                               UserEncryptionConfiguration encryption,
                               UserIndustrialConfiguration industrial,
                               UserAuthenticationConfiguration authentication,
                               UserSubscriptionConfiguration subscription,
                               UserSessionConfiguration session) {
}
