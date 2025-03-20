package org.kopingenieria.application.db.repository;

import org.kopingenieria.application.db.entity.MonitoringEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MonitoringEventRepository extends JpaRepository<MonitoringEvent, Long> {
    MonitoringEvent findByNodeId(String nodeId);
    MonitoringEvent findByEventType(String eventType);
    List<MonitoringEvent> findByEventName(String eventName);
    List<MonitoringEvent> findBySeverity(Integer severity);
    List<MonitoringEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    @Query("SELECT e FROM MonitoringEvent e WHERE e.sourceNode = :node " +
            "AND e.timestamp >= :startTime ORDER BY e.timestamp DESC")
    List<MonitoringEvent> findRecentEventsByNode(
            @Param("node") String node,
            @Param("startTime") LocalDateTime startTime
    );
}
