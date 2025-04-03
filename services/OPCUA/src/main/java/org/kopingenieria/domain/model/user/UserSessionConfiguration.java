package org.kopingenieria.domain.model.user;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.communication.SessionStatus;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.locale.LocaleIds;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.kopingenieria.domain.enums.security.SecurityPolicyUri;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserSessionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 4L;

    private String sessionName;
    private String serverUri;
    private Long maxResponseMessageSize;
    private MessageSecurityMode securityMode;
    private SecurityPolicyUri securityPolicyUri;
    private SecurityPolicy securityPolicy;
    private String clientCertificate;
    private String serverCertificate;
    private List<LocaleIds> localeIds;
    private Integer maxChunkCount;
    private final Timeouts timeout=Timeouts.SESSION;
    private SessionStatus sessionStatus;
    private LocalDateTime lastActivity;

}
