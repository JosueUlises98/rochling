package org.kopingenieria.repository;

import org.kopingenieria.exceptions.AuditException;
import org.kopingenieria.model.AuditEntryType;
import org.kopingenieria.model.dto.AuditEventDTO;
import org.kopingenieria.model.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent,String> {

    @Query(value = "SELECT * FROM audit_events ae WHERE ae.event_type = :type", nativeQuery = true)
    List<AuditEvent> findByType(@Param("type") AuditEntryType type) throws AuditException;

    @Query(value = "SELECT * FROM audit_events ae WHERE ae.timestamp BETWEEN :start AND :end", nativeQuery = true)
    List<AuditEvent> findByTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    ) throws AuditException;

    @Query(value = "SELECT * FROM audit_events ae WHERE ae.user_id = :username", nativeQuery = true)
    List<AuditEvent> findByUsername(@Param("username") String username) throws AuditException;

    @Query(value = "SELECT * FROM audit_events ae WHERE ae.class_name = :className AND ae.method_name = :methodName", nativeQuery = true)
    List<AuditEvent> findByClassAndMethod(
            @Param("className") String className,
            @Param("methodName") String methodName
    ) throws AuditException;

    @Query(value = """
        SELECT * FROM audit_events ae WHERE 
           (:startDate IS NULL OR ae.timestamp >= :startDate) AND 
        (:endDate IS NULL OR ae.timestamp <= :endDate)
        """, nativeQuery = true)
    Page<AuditEvent> findAuditEvents(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    ) throws AuditException;

    @Query(value = """
        SELECT * FROM audit_events ae WHERE 
        (:userId IS NULL OR ae.user_id = :userId) AND 
        (:eventType IS NULL OR ae.event_type = :eventType) AND 
        (:component IS NULL OR ae.component = :component) AND 
        ae.timestamp BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    Page<AuditEvent> searchEvents(
            @Param("userId") String userId,
            @Param("eventType") AuditEntryType eventType,
            @Param("component") String component,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    ) throws AuditException;

    @Query(value = "DELETE FROM audit_events ae WHERE ae.timestamp < :beforeDate", nativeQuery = true)
    long deleteByTimestampBefore(@Param("beforeDate") LocalDateTime beforeDate) throws AuditException;

    @Query(value = "SELECT COUNT(*) FROM audit_events ae WHERE ae.event_type = :eventType", nativeQuery = true)
    long countByEventType(@Param("eventType") AuditEntryType eventType) throws AuditException;

    @Query(value = "SELECT COUNT(*) FROM audit_events ae WHERE ae.user_id = :userId", nativeQuery = true)
    long countByUserId(@Param("userId") String userId) throws AuditException;

    @Query(value = "SELECT COUNT(*) FROM audit_events ae WHERE ae.timestamp BETWEEN :startDate AND :endDate", nativeQuery = true)
    long countByTimestampBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    ) throws AuditException;
}
