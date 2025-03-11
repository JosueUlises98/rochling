package org.kopingenieria.repository;

import org.kopingenieria.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent,String> {

    List<AuditEvent> findByType(AuditEntryType type);
    List<AuditEntry> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AuditEntry> findByUsername(String username);

    @Query("SELECT a FROM AuditEntry a WHERE a.className = :className AND a.methodName = :methodName")
    List<AuditEntry> findByClassAndMethod(String className, String methodName);
}
