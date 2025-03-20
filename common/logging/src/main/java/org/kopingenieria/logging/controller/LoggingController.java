package org.kopingenieria.logging.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.logging.exception.LogWriteException;
import org.kopingenieria.logging.exception.LogWriteFailureEvent;
import org.kopingenieria.logging.model.*;
import org.kopingenieria.domain.*;
import org.logging.model.*;
import org.kopingenieria.logging.service.LoggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/logs")
@Slf4j
@RequiredArgsConstructor
public class LoggingController {

    private final LoggingService loggingService;

    @PostMapping("/batch")
    public ResponseEntity<BatchLogResponse> logBatch(
            @Valid @RequestBody BatchLogRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        log.info("Inicio de procesamiento de lote de logs.");
        try {
            // Validación del lote
            if (request.getEntries() == null || request.getEntries().isEmpty()) {
                log.warn("El lote de logs está vacío. CorrelationId: {}", correlationId);
                throw new LogWriteFailureEvent("El lote de logs está vacío");
            }

            String batchId = generateBatchId();
            log.debug("Lote de logs recibido. BatchId: {}, CorrelationId: {}, Cantidad de entradas: {}",
                    batchId, correlationId, request.getEntries().size());

            List<LogEntry> entries = request.getEntries().stream()
                    .map(entry -> {
                        try {
                            return mapToLogEntry(entry, batchId, correlationId);
                        } catch (IllegalArgumentException e) {
                            log.error("Error al mapear la entrada del log: {}", e.getMessage(), e);
                            throw new LogWriteException("Error en formato de entrada: " + e.getMessage());
                        }
                    })
                    .toList();

            // Procesamiento del lote
            try {
                entries.forEach(loggingService::log);
                log.info("Lote de logs procesado con éxito. BatchId: {}, Cantidad procesada: {}", batchId, entries.size());
            } catch (Exception e) {
                log.error("Error al procesar el lote de logs. BatchId: {}, Error: {}", batchId, e.getMessage(), e);
                throw new LogWriteFailureEvent("Error al procesar el lote: " + e.getMessage());
            }

            return ResponseEntity
                    .accepted()
                    .body(new BatchLogResponse(batchId, entries.size()));

        } catch (LogWriteException | LogWriteFailureEvent e) {
            log.error("Error en procesamiento batch. CorrelationId: {}, Error: {}", correlationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en procesamiento batch. CorrelationId: {}, Error: {}", correlationId, e.getMessage(), e);
            throw new LogWriteException("Error inesperado al procesar el lote");
        }
    }

    @PostMapping("/immediate")
    public ResponseEntity<LogResponse> logImmediate(
            @Valid @RequestBody LogRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        log.info("Inicio de procesamiento de log inmediato.");
        try {
            // Validación de la entrada
            validateLogRequest(request);
            log.debug("Log recibido para procesamiento inmediato. CorrelationId: {}", correlationId);

            LogEntry logEntry = mapToLogEntry(request, generateBatchId(), correlationId);

            try {
                loggingService.logImmediate(logEntry);
                log.info("Log inmediato procesado exitosamente. LogId: {}", logEntry.getId());
            } catch (Exception e) {
                log.error("Error al procesar log inmediato. CorrelationId: {}, Error: {}", correlationId, e.getMessage(), e);
                throw new LogWriteException("Error al escribir log inmediato: " + e.getMessage());
            }
            return ResponseEntity.ok(
                    new LogResponse(logEntry.getId(), "Log registrado exitosamente")
            );
        } catch (LogWriteException e) {
            log.error("Error en escritura de log inmediato. CorrelationId: {}, Error: {}", correlationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado en log inmediato. CorrelationId: {}, Error: {}", correlationId, e.getMessage(), e);
            throw new LogWriteFailureEvent("Error inesperado al procesar log inmediato");
        }
    }

    private void validateLogRequest(LogRequest request) {
        log.debug("Validando entrada de log.");
        if (request == null) {
            log.warn("Solicitud de log es nula.");
            throw new LogWriteException("La solicitud no puede ser nula");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            log.warn("El mensaje del log es requerido.");
            throw new LogWriteException("El mensaje del log es requerido");
        }
        try {
            LogLevel.valueOf(request.getLevel());
        } catch (IllegalArgumentException e) {
            log.warn("Nivel de log inválido: {}", request.getLevel());
            throw new LogWriteException("Nivel de log inválido: " + request.getLevel());
        }
        log.debug("Entrada de log validada exitosamente.");
    }

    private LogEntry mapToLogEntry(LogRequest request, String batchId, String correlationId) {
        log.debug("Mapeando LogRequest a LogEntry. BatchId: {}, CorrelationId: {}", batchId, correlationId);
        try {
            LogEntry logEntry = LogEntry.builder()
                    .id(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .level(LogLevel.valueOf(request.getLevel()))
                    .message(request.getMessage())
                    .source(request.getSource())
                    .batchId(batchId)
                    .correlationId(correlationId)
                    .additionalInfo(request.getAdditionalInfo())
                    .build();
            log.debug("LogEntry mapeado exitosamente. LogId: {}", logEntry.getId());
            return logEntry;
        } catch (Exception e) {
            log.error("Error al mapear la entrada del log. BatchId: {}, Error: {}", batchId, e.getMessage(), e);
            throw new LogWriteException("Error al mapear la entrada del log: " + e.getMessage());
        }
    }

    private String generateBatchId() {
        log.debug("Generando BatchId.");
        return UUID.randomUUID().toString();
    }
}


