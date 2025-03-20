package org.kopingenieria.application.db.repository;

import org.kopingenieria.application.db.entity.OpcUaSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpcUaSessionRepository extends JpaRepository<OpcUaSession, Long> {
    Optional<OpcUaSession> findBySessionName(String sessionName);
    List<OpcUaSession> findByStatus(String status);
    @Query("SELECT s FROM OpcUaSession s WHERE s.expirationTime < :now")
    List<OpcUaSession> findExpiredSessions(@Param("now") LocalDateTime now);
}
