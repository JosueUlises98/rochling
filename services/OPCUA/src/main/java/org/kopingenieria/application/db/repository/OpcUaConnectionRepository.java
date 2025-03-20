package org.kopingenieria.application.db.repository;

import org.kopingenieria.application.db.entity.OpcUaConnection;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpcUaConnectionRepository extends JpaRepository<OpcUaConnection, Long> {
    List<OpcUaConnection> findByStatus(ConnectionStatus status);
    Optional<OpcUaConnection> findByEndpointUrl(String endpointUrl);
    @Query("SELECT c FROM OpcUaConnection c WHERE c.quality.healthStatus = :status")
    List<OpcUaConnection> findByHealthStatus(@Param("status") String status);
}
