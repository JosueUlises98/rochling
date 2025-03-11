package org.kopingenieria.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.elasticsearch.client.RestClient;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "logging")
@Slf4j
public class LoggingConfig {

    private Elasticsearch elasticsearch = new Elasticsearch();
    private File file = new File();
    private Retry retry = new Retry();
    private Metrics metrics = new Metrics();

    @Data
    public static class Elasticsearch {
        private String indexPrefix = "logs";
        private String hosts = "localhost:9200";
        private String username;
        private String password;
        private int bulkSize = 1000;
        private int flushIntervalSeconds = 5;
    }

    @Data
    public static class File {
        private String filename = "application.log";
        private String directory = "C:/Users/Public/Documents/rochling/logs";
        private String datePattern = "yyyy-MM-dd";
        private boolean json = true;
        private long maxFileSize = 10485760; // 10MB
        private int maxHistory = 30;
    }

    @Data
    public static class Retry {
        private int maxAttempts = 3;
        private long delay = 1000;
        private boolean exponentialBackoff = true;
    }

    @Data
    public static class Metrics {
        private boolean enabled = true;
        private long performanceThreshold = 1000;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() throws URISyntaxException {
        // Configuración del cliente Elasticsearch
        RestClient restClient = RestClient.builder(
                        String.valueOf(HttpHost.create(elasticsearch.getHosts()))
                )
                .setDefaultHeaders(createAuthHeaders())
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                                .setConnectTimeout(5000)
                                .setSocketTimeout(60000))
                .build();
        return new ElasticsearchClient(
                new RestClientTransport(
                        restClient,
                        new JacksonJsonpMapper()
                )
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Bean
    public File fileConfiguration() {
        File dynamicFile = new File();
        dynamicFile.setFilename(file.getFilename());
        dynamicFile.setDirectory(file.getDirectory());
        dynamicFile.setDatePattern(file.getDatePattern());
        dynamicFile.setJson(file.isJson());
        dynamicFile.setMaxFileSize(file.getMaxFileSize());
        dynamicFile.setMaxHistory(file.getMaxHistory());
        return dynamicFile;
    }

    @Bean
    public Retry retryConfiguration() {
        Retry dynamicRetry = new Retry();
        dynamicRetry.setMaxAttempts(retry.getMaxAttempts());
        dynamicRetry.setDelay(retry.getDelay());
        dynamicRetry.setExponentialBackoff(retry.isExponentialBackoff());
        return dynamicRetry;
    }

    @Bean
    public Metrics metricsConfiguration() {
        Metrics dynamicMetrics = new Metrics();
        dynamicMetrics.setEnabled(metrics.isEnabled());
        dynamicMetrics.setPerformanceThreshold(metrics.getPerformanceThreshold());
        return dynamicMetrics;
    }

    @Bean
    public LoggingConfig loggingConfiguration() {
        return createDefaultConfig();
    }

    private LoggingConfig createDefaultConfig() {
        LoggingConfig loggingConfig = new LoggingConfig();
        
        // Initialize Elasticsearch configuration
        LoggingConfig.Elasticsearch elasticsearchConfig = new LoggingConfig.Elasticsearch();
        elasticsearchConfig.setIndexPrefix("logs");
        elasticsearchConfig.setHosts("localhost:9200");
        elasticsearchConfig.setUsername(null);
        elasticsearchConfig.setPassword(null);
        elasticsearchConfig.setBulkSize(1000);
        elasticsearchConfig.setFlushIntervalSeconds(5);
        loggingConfig.elasticsearch = elasticsearchConfig;
        
        // Initialize File configuration
        LoggingConfig.File fileConfig = new LoggingConfig.File();
        fileConfig.setFilename("application.log");
        fileConfig.setDirectory("C:/Users/Public/Documents/rochling/logs");
        fileConfig.setDatePattern("yyyy-MM-dd");
        fileConfig.setJson(true);
        fileConfig.setMaxFileSize(10485760);
        fileConfig.setMaxHistory(30);
        loggingConfig.file = fileConfig;
        
        // Initialize Retry configuration
        LoggingConfig.Retry retryConfig = new LoggingConfig.Retry();
        retryConfig.setMaxAttempts(3);
        retryConfig.setDelay(1000);
        retryConfig.setExponentialBackoff(true);
        loggingConfig.retry = retryConfig;
        
        // Initialize Metrics configuration
        LoggingConfig.Metrics metricsConfig = new LoggingConfig.Metrics();
        metricsConfig.setEnabled(true);
        metricsConfig.setPerformanceThreshold(1000);
        loggingConfig.metrics = metricsConfig;
    
        return loggingConfig;
    }

    private org.apache.http.Header[] createAuthHeaders() {
        if (elasticsearch.getUsername() != null && elasticsearch.getPassword() != null) {
            String auth = elasticsearch.getUsername() + ":" + elasticsearch.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            return new Header[]{new BasicHeader("Authorization", "Basic " + encodedAuth)};
        }
        return new Header[]{};
    }
    
}
