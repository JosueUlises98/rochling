package org.kopingenieria.application.db.repository;

import org.kopingenieria.application.db.entity.OpcUaAuthentication;
import org.kopingenieria.domain.enums.communication.MessageSecurityMode;
import org.kopingenieria.domain.enums.connection.SecurityPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OpcUaAuthenticationRepository extends JpaRepository<OpcUaAuthentication, Long> {
    List<OpcUaAuthentication> findBySecurityPolicy(SecurityPolicy securityPolicy);
    List<OpcUaAuthentication> findByMessageSecurityMode(MessageSecurityMode mode);
}
