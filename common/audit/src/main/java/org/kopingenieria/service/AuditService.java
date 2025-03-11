package org.kopingenieria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.model.AuditEntryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditService(AuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    public AuditEntry createAuditEntry(AuditEntryType type, String className,
                                       String methodName, String message, Map<String, Object> metadata) {
        try {
            AuditEntry entry = AuditEntry.builder()
                    .timestamp(LocalDateTime.now())
                    .type(type)
                    .className(className)
                    .methodName(methodName)
                    .message(message)
                    .username(SecurityContextHolder.getContext().getAuthentication().getName())
                    .metadata(objectMapper.writeValueAsString(metadata))
                    .build();

            return auditRepository.save(entry);
        } catch (Exception e) {
            log.error("Error creating audit entry: {}", e.getMessage());
            throw new AuditingException("Failed to create audit entry", e);
        }
    }

    public List<AuditEntry> searchAuditLogs(AuditSearchCriteria criteria) {
        try {
            if (criteria.getType() != null) {
                return auditRepository.findByType(criteria.getType());
            } else if (criteria.getStartDate() != null && criteria.getEndDate() != null) {
                return auditRepository.findByTimestampBetween(
                        criteria.getStartDate(), criteria.getEndDate());
            }
            return auditRepository.findAll();
        } catch (Exception e) {
            log.error("Error searching audit logs: {}", e.getMessage());
            throw new AuditingException("Failed to search audit logs", e);
        }
    }
}
