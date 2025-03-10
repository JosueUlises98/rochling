package org.kopingenieria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.model.LogEntry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileLogWriter {

    private final ObjectMapper objectMapper;
    private final LoggingConfiguration config;

    public void write(LogEntry logEntry) {
        try {
            String fileName = createFileName();
            File logFile = new File(config.getFile().getDirectory(), fileName);

            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }

            String logLine = config.getFile().isJson() ?
                    objectMapper.writeValueAsString(logEntry) :
                    formatLogEntry(logEntry);

            Files.writeString(
                    logFile.toPath(),
                    logLine + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            checkRotation(logFile);
        } catch (Exception e) {
            throw new LogWriteException("Failed to write to file", e);
        }
    }

    private String createFileName() {
        return "application-" +
                LocalDate.now().format(DateTimeFormatter.ofPattern(
                        config.getFile().getDatePattern()
                )) +
                (config.getFile().isJson() ? ".json" : ".log");
    }

    private void checkRotation(File logFile) {
        if (logFile.length() > config.getFile().getMaxFileSize()) {
            rotateFile(logFile);
        }
    }

    private void rotateFile(File logFile) {
        // Implementación de rotación de archivos
    }

    private String formatLogEntry(LogEntry entry) {
        // Implementación de formato de texto plano
        return String.format("%s [%s] %s: %s",
                entry.getTimestamp(),
                entry.getLevel(),
                entry.getType(),
                entry.getDescription()
        );
    }
}
