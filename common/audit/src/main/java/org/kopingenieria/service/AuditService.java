package org.kopingenieria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.exceptions.AuditPersistenceException;
import org.kopingenieria.mapper.AuditMapper;
import org.kopingenieria.model.LogLevel;
import org.kopingenieria.model.LogSystemEvent;
import org.kopingenieria.model.dto.AuditEventDTO;
import org.kopingenieria.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository repository;
    private final AuditMapper mapper;

    @Async("auditExecutor")
    @Transactional
    @LogSystemEvent(event = "Audit",description = "Async Audit event", level = LogLevel.DEBUG)
    public void registerAsyncEvent(AuditEventDTO eventDTO) throws AuditPersistenceException {
        try {
            repository.save(mapper.toEntity(eventDTO));
        } catch (Exception e) {
            throw new AuditPersistenceException("Error persisting audit event", e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Find user",description = "Find user by id", level = LogLevel.DEBUG)
    public List<AuditEventDTO> findByUser(String userId) {
        return repository.findByUsername(userId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Find events",description = "Find events by date range", level = LogLevel.DEBUG)
    public Page<AuditEventDTO> findEvents(String userId, LocalDateTime startDate,
                                          LocalDateTime endDate, Pageable pageable) {
        return repository.findAuditEvents(userId, startDate, endDate, pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Find event",description = "Find event by id", level = LogLevel.DEBUG)
    public Optional<AuditEventDTO> findById(String id) {
        return repository.findById(id).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Search events",description = "Search events by date range", level = LogLevel.DEBUG)
    public Page<AuditEventDTO> searchEvents(String userId, String eventType,
                                            String component, LocalDateTime startDate, LocalDateTime endDate,
                                            Pageable pageable) {
        return repository.searchEvents(userId, eventType, component, startDate,
                endDate, pageable).map(mapper::toDto);
    }

    @Async("auditExecutor")
    @Transactional
    @LogSystemEvent(event = "Delete old events",description = "Delete old events", level = LogLevel.DEBUG)
    public void deleteOldEvents(LocalDateTime beforeDate) throws AuditPersistenceException {
        try {
            repository.deleteByTimestampBefore(beforeDate);
        } catch (Exception e) {
            throw new AuditPersistenceException("Error deleting old audit events", e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Count events",description = "Count events by type", level = LogLevel.DEBUG)
    public long countEventsByType(String eventType) {
        return repository.countByEventType(eventType);
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Count events",description = "Count events by user", level = LogLevel.DEBUG)
    public long countEventsByUser(String userId) {
        return repository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Count events",description = "Count events by date range", level = LogLevel.DEBUG)
    public long countEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return repository.countByTimestampBetween(startDate, endDate);
    }
}
