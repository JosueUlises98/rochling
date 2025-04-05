package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.kopingenieria.domain.enums.communication.SessionStatus;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.locale.LocaleIds;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.kopingenieria.domain.enums.security.SecurityPolicyUri;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class DefaultSessionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 4L;

    private final String sessionName;
    private final String serverUri;
    private final Long maxResponseMessageSize;
    private final List<LocaleIds> localeIds;
    private final Integer maxChunkCount;
    private final Timeouts timeout=Timeouts.SESSION;
    @Setter
    private SessionStatus sessionStatus;
}
