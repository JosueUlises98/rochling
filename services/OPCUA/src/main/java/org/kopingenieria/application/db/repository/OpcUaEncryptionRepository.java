package org.kopingenieria.application.db.repository;

import org.kopingenieria.application.db.entity.OpcUaEncryption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpcUaEncryptionRepository extends JpaRepository<OpcUaEncryption, Long> {
    List<OpcUaEncryption> findBySecurityPolicy(String securityPolicy);
    List<OpcUaEncryption> findByMessageSecurityMode(String messageSecurityMode);
}
