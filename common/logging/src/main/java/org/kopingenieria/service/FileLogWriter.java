package org.kopingenieria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.config.InfraestructureConfig;
import org.kopingenieria.exception.LogWriteException;
import org.kopingenieria.model.LogEntry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileLogWriter {

    private final ObjectMapper objectMapper;
    private final InfraestructureConfig.FileConfig config;

    public void write(LogEntry logEntry) {
        try {
            String fileName = createFileName();
            File logFile = new File(config.getBasePath(), fileName);

            if (!logFile.getParentFile().exists()) {
                boolean mkdirs = logFile.getParentFile().mkdirs();
                if (!mkdirs) {
                    throw new LogWriteException("Failed to create log directory");
                }
            }
            if (!logFile.exists()) {
                boolean newFile = logFile.createNewFile();
                if (!newFile) {
                    throw new LogWriteException("Failed to create log file");
                }
            }
            String logLine = config.getIsJson() ?
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
                LocalDate.now().format(DateTimeFormatter.ofPattern(config.getNamePattern()));
    }

    private void checkRotation(File logFile) {
        if (logFile.length() > config.getMaxFileSize()) {
            rotateFile(logFile);
        }
    }

    private void rotateFile(File logFile) {
        try {
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            File rotatedFile = new File(logFile.getParent(), logFile.getName().replaceFirst("(?<=\\.)[^.]+$", timestamp + ".$0"));
            
            boolean renamed = logFile.renameTo(rotatedFile);
            if (!renamed) {
                throw new LogWriteException("Failed to rename log file for rotation");
            }
    
            boolean newFileCreated = logFile.createNewFile();
            if (!newFileCreated) {
                throw new LogWriteException("Failed to create new log file after rotation");
            }
        } catch (Exception e) {
            throw new LogWriteException("Error during log file rotation", e);
        }
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
