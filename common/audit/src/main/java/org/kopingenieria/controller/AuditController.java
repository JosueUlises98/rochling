package org.kopingenieria.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kopingenieria.exceptions.AuditPersistenceException;
import org.kopingenieria.model.AuditEntryType;
import org.kopingenieria.model.annotation.Auditable;
import org.kopingenieria.model.dto.AuditEventDTO;
import org.kopingenieria.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(type = AuditEntryType.CREATE, includeParams = true)
    public ResponseEntity<Void> registerEvent(@Valid @RequestBody AuditEventDTO eventDTO) throws AuditPersistenceException {
        auditService.registerAsyncEvent(eventDTO);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<List<AuditEventDTO>> findByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditService.findByUser(userId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditEventDTO>> searchEvents(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String component,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.searchEvents(userId, eventType, component,
                startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditEventDTO> findById(@PathVariable String id) {
        return auditService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOldEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) throws AuditPersistenceException {
        auditService.deleteOldEvents(beforeDate);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/stats/type/{eventType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countByEventType(@PathVariable String eventType) {
        return ResponseEntity.ok(auditService.countEventsByType(eventType));
    }

    @GetMapping("/stats/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditService.countEventsByUser(userId));
    }

    @GetMapping("/stats/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(auditService.countEventsByDateRange(startDate, endDate));
    }
}
