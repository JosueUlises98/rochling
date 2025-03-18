package org.kopingenieria.logging.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.logging.model.LogEntry;
import org.kopingenieria.logging.service.ElasticSearchLogWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class BulkLogProcessor {

    private final ElasticSearchLogWriter elasticsearchWriter;
    private final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${logging.elasticsearch.bulk-size:1000}")
    private int bulkSize;

    @Value("${logging.elasticsearch.flush-interval-seconds:5}")
    private int flushIntervalSeconds;

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(
                this::processBulk,
                flushIntervalSeconds,
                flushIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    public void addLogEntry(LogEntry entry) {
        logQueue.offer(entry);
        if (logQueue.size() >= bulkSize) {
            processBulk();
        }
    }

    @Scheduled(fixedRateString = "${logging.elasticsearch.flush-interval-seconds:5}000")
    public void processBulk() {
        List<LogEntry> batch = new ArrayList<>();
        logQueue.drainTo(batch, bulkSize);

        if (!batch.isEmpty()) {
            try {
                elasticsearchWriter.writeBulk(batch);
            } catch (Exception e) {
                log.error("Error processing bulk log entries", e);
                batch.forEach(entry -> handleFailedEntry(entry, e));
            }
        }
    }

    private void handleFailedEntry(LogEntry entry, Exception e) {
        entry.setRetryCount(entry.getRetryCount() + 1);
        if (entry.getRetryCount() < 3) {
            logQueue.offer(entry);
        } else {
            log.error("Failed to process log entry after 3 attempts", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
