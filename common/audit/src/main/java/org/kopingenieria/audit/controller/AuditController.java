package org.kopingenieria.audit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kopingenieria.audit.exceptions.AuditPersistenceException;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.logging.model.LogException;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogRestCall;
import org.kopingenieria.audit.model.dto.AuditEventDTO;
import org.kopingenieria.audit.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @LogRestCall(event = "Register Event", description = "Registers a new audit event", level = LogLevel.DEBUG)
    @LogException(message = "Error while registering event", level = LogLevel.ERROR, exceptions = {AuditPersistenceException.class}, method = "registerEvent")
    @PostMapping
    public ResponseEntity<Void> registerEvent(@Valid @RequestBody AuditEventDTO eventDTO) throws AuditPersistenceException {
            auditService.registerAsyncEvent(eventDTO);
        return ResponseEntity.accepted().build();
    }

    @LogRestCall(event = "Find Events By User", description = "Fetches audit events by user ID", level = LogLevel.DEBUG)
    @LogException(message = "Error while fetching events by user", level = LogLevel.ERROR, exceptions = {AuditPersistenceException.class}, method = "findByUser")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditEventDTO>> findByUser(@PathVariable String userId) throws AuditPersistenceException {
            return ResponseEntity.ok(auditService.findByUser(userId));
    }

    @LogRestCall(event = "Search Events", description = "Searches audit events with filters", level = LogLevel.DEBUG)
    @LogException(message = "Error while searching events", level = LogLevel.ERROR, exceptions = {AuditPersistenceException.class}, method = "searchEvents")
    @GetMapping("/search")
    public ResponseEntity<Page<AuditEventDTO>> searchEvents(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) AuditEntryType eventType,
            @RequestParam(required = false) String component,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) throws AuditPersistenceException {
            return ResponseEntity.ok(auditService.searchEvents(userId, eventType, component,
                    startDate, endDate, pageable));
    }

    @LogRestCall(event = "Find Event By ID", description = "Fetches audit event by its ID", level = LogLevel.DEBUG)
    @GetMapping("/{id}")
    public ResponseEntity<AuditEventDTO> findById(@PathVariable String id) {
            return auditService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @LogRestCall(event = "Delete Old Events", description = "Deletes events before a specific date", level = LogLevel.DEBUG)
    @LogException(message = "Error while deleting old events", level = LogLevel.ERROR, exceptions = {AuditPersistenceException.class}, method = "deleteOldEvents")
    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> deleteOldEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) throws AuditPersistenceException {
            auditService.deleteOldEvents(beforeDate);
        return ResponseEntity.accepted().build();
    }

    @LogRestCall(event = "Count Events By Type", description = "Counts audit events by their type", level = LogLevel.DEBUG)
    @LogException(message = "Error while counting events by type", level = LogLevel.ERROR, exceptions = {AuditPersistenceException.class}, method = "countByEventType")
    @GetMapping("/stats/type/{eventType}")
    public ResponseEntity<Long> countByEventType(@PathVariable AuditEntryType eventType) throws AuditPersistenceException {
            return ResponseEntity.ok(auditService.countEventsByType(eventType));
    }

    @LogRestCall(event = "Count Events By User", description = "Counts audit events for a user", level = LogLevel.DEBUG)
    @LogException(message = "Error while counting events by user", level = LogLevel.ERROR, exceptions = {AuditPersistenceException.class}, method = "countByUser")
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Long> countByUser(@PathVariable String userId) throws AuditPersistenceException {
            return ResponseEntity.ok(auditService.countEventsByUser(userId));
    }

    @LogRestCall(event = "Count Events By Date Range", description = "Counts audit events within a date range", level = LogLevel.DEBUG)
    @LogException(message = "Error while counting events by date range", level = LogLevel.ERROR, exceptions = {AuditPersistenceException.class}, method = "countByDateRange")
    @GetMapping("/stats/date-range")
    public ResponseEntity<Long> countByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) throws AuditPersistenceException {
            return ResponseEntity.ok(auditService.countEventsByDateRange(startDate, endDate));
        }
}
