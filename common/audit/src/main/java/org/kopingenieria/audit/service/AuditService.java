package org.kopingenieria.audit.service;

import lombok.RequiredArgsConstructor;
import org.kopingenieria.audit.exceptions.AuditException;
import org.kopingenieria.audit.exceptions.AuditPersistenceException;
import org.kopingenieria.audit.exceptions.MappingException;
import org.kopingenieria.audit.mapper.AuditMapper;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import org.kopingenieria.audit.model.dto.AuditEventDTO;
import org.kopingenieria.audit.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository repository;
    private final AuditMapper mapper;

    @Async("auditExecutor")
    @Transactional
    @LogSystemEvent(event = "Audit", description = "Async Audit event", level = LogLevel.DEBUG)
    public void registerAsyncEvent(AuditEventDTO eventDTO) throws AuditPersistenceException {
        try {
            repository.save(mapper.toEntity(eventDTO));
        } catch (Exception e) {
            throw new AuditPersistenceException("Invalid event data provided for saving.", e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Find user", description = "Find user by id", level = LogLevel.DEBUG)
    public List<AuditEventDTO> findByUser(String userId) throws AuditPersistenceException {
        try {
            return repository.findByUsername(userId).stream()
                    .map(auditEvent -> {
                        try {
                            return mapper.toDto(auditEvent);
                        } catch (MappingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (AuditException e) {
            throw new AuditPersistenceException("Error finding audit events for user: " + userId, e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Find events", description = "Find events by date range", level = LogLevel.DEBUG)
    public Page<AuditEventDTO> findEvents(String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) throws AuditPersistenceException {
        try {
            return repository.findAuditEvents(userId, startDate, endDate, pageable)
                    .map(auditEvent -> {
                        try {
                            return mapper.toDto(auditEvent);
                        } catch (MappingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (AuditException e) {
            throw new AuditPersistenceException("Error finding events for the specified date range.", e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Find event", description = "Find event by id", level = LogLevel.DEBUG)
    public Optional<AuditEventDTO> findById(String id) {
        return repository.findById(id).map(auditEvent -> {
            try {
                return mapper.toDto(auditEvent);
            } catch (MappingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Search events", description = "Search events by filters", level = LogLevel.DEBUG)
    public Page<AuditEventDTO> searchEvents(String userId, AuditEntryType eventType, String component, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) throws AuditPersistenceException {
        try {
            return repository.searchEvents(userId, eventType, component, startDate, endDate, pageable)
                    .map(auditEvent -> {
                        try {
                            return mapper.toDto(auditEvent);
                        } catch (MappingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (AuditException e) {
            throw new AuditPersistenceException("Error searching audit events with the specified filters.", e);
        }
    }

    @Async("auditExecutor")
    @Transactional
    @LogSystemEvent(event = "Delete old events", description = "Delete old events", level = LogLevel.DEBUG)
    public void deleteOldEvents(LocalDateTime beforeDate) throws AuditPersistenceException {
        try {
            repository.deleteByTimestampBefore(beforeDate);
        } catch (AuditException e) {
            throw new AuditPersistenceException("Invalid date provided for deletion: " + beforeDate, e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Count events", description = "Count events by type", level = LogLevel.DEBUG)
    public long countEventsByType(AuditEntryType eventType) throws AuditPersistenceException {
        try {
            return repository.countByEventType(eventType);
        } catch (AuditException e) {
            throw new AuditPersistenceException("Error counting events by type: " + eventType, e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Count events", description = "Count events by user", level = LogLevel.DEBUG)
    public long countEventsByUser(String userId) throws AuditPersistenceException {
        try {
            return repository.countByUserId(userId);
        } catch (AuditException e) {
            throw new AuditPersistenceException("Error counting events for user: " + userId, e);
        }
    }

    @Transactional(readOnly = true)
    @LogSystemEvent(event = "Count events", description = "Count events by date range", level = LogLevel.DEBUG)
    public long countEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws AuditPersistenceException {
        try {
            return repository.countByTimestampBetween(startDate, endDate);
        } catch (AuditException e) {
            throw new AuditPersistenceException("Error counting events for the specified date range.", e);
        }
    }
}
