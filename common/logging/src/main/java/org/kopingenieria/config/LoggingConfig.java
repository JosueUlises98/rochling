package org.kopingenieria.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.processor.BulkLogProcessor;
import org.kopingenieria.service.ElasticSearchLogWriter;
import org.kopingenieria.service.FileLogWriter;
import org.kopingenieria.service.LoggingService;
import org.kopingenieria.util.LogEntryEnricher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@Slf4j
public class LoggingConfig {

    private static final String APP_NAME = "logging";
    private static final String APP_VERSION = "1.0.0";
    private static final String APP_DESCRIPTION = "Logging Service";

    @Bean
    public LoggingService loggingService(
            ElasticSearchLogWriter elasticSearchLogWriter,
            FileLogWriter fileLogWriter,
            LogEntryEnricher logEntryEnricher,
            BulkLogProcessor bulkLogProcessor,
            ApplicationEventPublisher eventPublisher) {
        log.info("Configurando LoggingService");
        return new LoggingService(
                elasticSearchLogWriter,
                fileLogWriter,
                logEntryEnricher,
                bulkLogProcessor,
                eventPublisher
        );
    }
}
