package org.kopingenieria.logging.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.logging.processor.BulkLogProcessor;
import org.kopingenieria.logging.service.ElasticSearchLogWriter;
import org.kopingenieria.logging.service.FileLogWriter;
import org.kopingenieria.logging.service.LoggingService;
import org.kopingenieria.logging.util.LogEntryEnricher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@Slf4j
public class LoggingConfig {

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
