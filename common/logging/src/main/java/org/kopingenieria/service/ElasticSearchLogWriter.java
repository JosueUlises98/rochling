package org.kopingenieria.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.exception.LogWriteException;
import org.kopingenieria.model.LogEntry;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElasticSearchLogWriter {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;
    private final ElasticsearchProperties config;

    public void write(LogEntry logEntry) {
        try {
            String indexName = createIndexName();
            elasticsearchClient.index(i -> i
                    .index(indexName)
                    .document(logEntry)
            );
        } catch (Exception e) {
            throw new LogWriteException("Failed to write to Elasticsearch", e);
        }
    }

    public void writeBulk(List<LogEntry> entries) {
        try {
            BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
            String indexName = createIndexName();

            for (LogEntry entry : entries) {
                bulkRequest.operations(op -> op
                        .index(idx -> idx
                                .index(indexName)
                                .document(entry)
                        )
                );
            }

            elasticsearchClient.bulk(bulkRequest.build());
        } catch (Exception e) {
            throw new LogWriteException("Failed to write bulk to Elasticsearch", e);
        }
    }

    private String createIndexName() {
        return config.getIndexPrefix() +
                "-" +
                LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }
}
