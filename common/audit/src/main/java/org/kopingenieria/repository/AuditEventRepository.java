package org.kopingenieria.repository;

import org.kopingenieria.model.AuditEntryType;
import org.kopingenieria.model.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent,String> {

    @Query(value = "SELECT * FROM audit_events ae WHERE ae.event_type = :type", nativeQuery = true)
    List<AuditEvent> findByType(@Param("type") AuditEntryType type);

    @Query(value = "SELECT * FROM audit_events ae WHERE ae.timestamp BETWEEN :start AND :end", nativeQuery = true)
    List<AuditEvent> findByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT * FROM audit_events ae WHERE ae.user_id = :username", nativeQuery = true)
    List<AuditEvent> findByUsername(@Param("username") String username);

    @Query(value = "SELECT * FROM audit_events a WHERE a.class_name = :className AND a.method_name = :methodName", nativeQuery = true)
    List<AuditEvent> findByClassAndMethod(String className, String methodName);

    @Query(value = "SELECT * FROM audit_events ae WHERE " +
            "(:userId IS NULL OR ae.user_id = :userId) AND " +
            "(:startDate IS NULL OR ae.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR ae.timestamp <= :endDate)", nativeQuery = true)
    Page<AuditEvent> findAuditEvents(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query(value = "SELECT * FROM audit_events a WHERE " +
            "(:userId IS NULL OR a.user_id = :userId) AND " +
            "(:eventType IS NULL OR a.event_type = :eventType) AND " +
            "(:component IS NULL OR a.component = :component) AND " +
            "a.timestamp BETWEEN :startDate AND :endDate", nativeQuery = true)
    Page<AuditEvent> searchEvents(
            @Param("userId") String userId,
            @Param("eventType") String eventType,
            @Param("component") String component,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query(value = "DELETE FROM audit_events WHERE timestamp < :beforeDate", nativeQuery = true)
    long deleteByTimestampBefore(LocalDateTime beforeDate);

    @Query(value = "SELECT COUNT(*) FROM audit_events WHERE event_type = :eventType", nativeQuery = true)
    long countByEventType(String eventType);

    @Query(value = "SELECT COUNT(*) FROM audit_events WHERE user_id = :userId", nativeQuery = true)
    long countByUserId(String userId);

    @Query(value = "SELECT COUNT(*) FROM audit_events WHERE timestamp BETWEEN :startDate AND :endDate", nativeQuery = true)
    long countByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
