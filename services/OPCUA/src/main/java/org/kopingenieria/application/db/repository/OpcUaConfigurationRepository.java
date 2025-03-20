package org.kopingenieria.application.db.repository;

import org.kopingenieria.application.db.entity.OpcUaConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpcUaConfigurationRepository extends JpaRepository<OpcUaConfiguration, Long> {
    boolean existsByName(String name);
    Optional<OpcUaConfiguration> findByName(String name);
    List<OpcUaConfiguration> findByEnabled(Boolean enabled);
    @Query("SELECT c FROM OpcUaConfiguration c LEFT JOIN FETCH c.subscriptions " +
            "LEFT JOIN FETCH c.monitoringEvents WHERE c.id = :id")
    Optional<OpcUaConfiguration> findByIdWithDetails(@Param("id") Long id);

}
