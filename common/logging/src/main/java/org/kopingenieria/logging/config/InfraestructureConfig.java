package org.kopingenieria.logging.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.kopingenieria.logging.model.LogLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
@Slf4j
@Data
@NoArgsConstructor(force = true)
public class InfraestructureConfig {

    private final Environment environment;
    private Elasticsearch elasticsearch = new Elasticsearch();
    private FileConfig fileConfig = new FileConfig();
    private Security security = new Security();
    private Performance performance = new Performance();
    private Database database = new Database();
    private Cache cache = new Cache();
    private Monitoring monitoring = new Monitoring();
    private Cdn cdn = new Cdn();
    private Redis redis = new Redis();
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private LoggingService loggingService = new LoggingService();

    @Data
    public static class Elasticsearch {
        private String indexPrefix;
        private List<String> hosts;
        private String username;
        private String password;
        private Integer bulkSize;
        private Integer flushInterval;
        private Boolean sslEnabled;
        private Integer numberOfShards;
        private Integer numberOfReplicas;
        private RetryConfig retry = new RetryConfig();
    }

    @Data
    public static class FileConfig {
        private String basePath;
        private String logPattern;
        private Integer maxFileSize;
        private Integer maxHistory;
        private Boolean compressBackups;
        private String namePattern;
        private LogLevel rootLevel;
        private LogLevel applicationLevel;
        private LocalDateTime datePattern;
        private Boolean isJson;
    }

    @Data
    public static class Security {
        private Boolean enableAudit;
        private String auditLogPath;
        private List<String> sensitiveFields;
        private Boolean maskSensitiveData;
    }

    @Data
    public static class Performance {
        private Boolean enableMetrics;
        private Integer bufferSize;
        private Integer queueSize;
        private Boolean asyncLogging;
    }

    @Data
    public static class Database {
        private String url;
        private String username;
        private String password;
        private HikariConfig hikari = new HikariConfig();
    }

    @Data
    public static class HikariConfig {
        private Integer maximumPoolSize;
        private Integer minimumIdle;
        private Integer connectionTimeout;
        private Integer idleTimeout;
        private Integer maxLifetime;
    }

    @Data
    public static class Cache {
        private String type;
        private CaffeineConfig caffeine = new CaffeineConfig();
    }

    @Data
    public static class CaffeineConfig {
        private String spec;
    }

    @Data
    public static class Monitoring {
        private NewRelicConfig newrelic = new NewRelicConfig();
        private DatadogConfig datadog = new DatadogConfig();
    }

    @Data
    public static class NewRelicConfig {
        private Boolean enabled;
        private String licenseKey;
    }

    @Data
    public static class DatadogConfig {
        private Boolean enabled;
        private String apiKey;
    }

    @Data
    public static class Cdn {
        private Boolean enabled;
        private String baseUrl;
        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class Redis {
        private String host;
        private Integer port;
        private String password;
        private Integer timeout;
        private Integer database;
    }

    @Data
    public static class Jwt {
        private String secret;
        private Long expiration;
        private RefreshToken refreshToken = new RefreshToken();
    }

    @Data
    public static class RefreshToken {
        private Long expiration;
    }

    @Data
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private String exposedHeaders;
        private Boolean allowCredentials;
        private Long maxAge;
    }

    @Data
    public static class RetryConfig {
        private Integer maxAttempts;
        private Long initialInterval;
        private Double multiplier;
        private Long maxInterval;
    }
    @Data
    public static class LoggingService {
        private Boolean enabled;
        private String apiKeyHeader;
        private List<String> apiKeys;
        private String allowedOrigins;
        private Processing processing = new Processing();
        private IndexManagement indexManagement = new IndexManagement();
        private FieldConfig fieldConfig = new FieldConfig();
    }

    @Data
    public static class Processing {
        private AsyncConfig async = new AsyncConfig();
        private BatchConfig batch = new BatchConfig();
    }

    @Data
    public static class AsyncConfig {
        private Boolean enabled;
        private Integer corePoolSize;
        private Integer maxPoolSize;
        private Integer queueCapacity;
    }

    @Data
    public static class BatchConfig {
        private Boolean enabled;
        private Integer size;
        private Integer timeout;
    }

    @Data
    public static class IndexManagement {
        private Boolean enabled;
        private String cleanupCron;
        private Integer retentionDays;
        private Integer hotPhaseDays;
        private Integer warmPhaseDays;
    }

    @Data
    public static class FieldConfig {
        private List<String> required;
        private List<String> masked;
        private Integer maxMessageSize;
        private Integer maxStacktraceLines;
    }

    @PostConstruct
    public void init() {
        String activeProfile = Arrays.toString(environment.getActiveProfiles());
        log.info("Initializing ApplicationConfig with profile: {}", activeProfile);
        validateConfiguration();
        configureSystem();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    public FileConfig fileConfigBean() {
        return fileConfig;
    }

    @Bean
    public Security securityBean() {
        return security;
    }

    @Bean
    public Performance performanceBean() {
        return performance;
    }

    @Bean
    public Database databaseBean() {
        return database;
    }

    @Bean
    public Cache cacheBean() {
        return cache;
    }

    @Bean
    public Monitoring monitoringBean() {
        return monitoring;
    }

    @Bean
    public Cdn cdnBean() {
        return cdn;
    }

    @Bean
    public Redis redisBean() {
        return redis;
    }

    @Bean
    public Jwt jwtBean() {
        return jwt;
    }

    @Bean
    public Cors corsBean() {
        return cors;
    }

    private void validateConfiguration() {
        Assert.notNull(elasticsearch.getHosts(), "Elasticsearch hosts no pueden ser null");
        Assert.notNull(fileConfig.getBasePath(), "Log base path no puede ser null");
        Assert.notNull(elasticsearch.getIndexPrefix(), "Index prefix no puede ser null");

        if (elasticsearch.getSslEnabled()) {
            Assert.notNull(elasticsearch.getUsername(), "Username requerido para conexión SSL");
            Assert.notNull(elasticsearch.getPassword(), "Password requerido para conexión SSL");
        }

        // Validaciones adicionales según el perfil activo
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            Assert.notNull(jwt.getSecret(), "JWT secret no puede ser null en producción");
            Assert.notNull(database.getUrl(), "Database URL no puede ser null en producción");
        }
    }

    private void configureSystem() {
        // Configuración de logging
        System.setProperty("LOG_PATH", fileConfig.getBasePath());
        System.setProperty("LOG_LEVEL_ROOT", fileConfig.getRootLevel().toString());
        System.setProperty("LOG_LEVEL_APP", fileConfig.getApplicationLevel().toString());

        // Configuración específica según el perfil
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            configureProdSystem();
        } else {
            configureDevSystem();
        }
    }

    private void configureProdSystem() {
        System.setProperty("elasticsearch.ssl.enabled", "true");
        System.setProperty("server.ssl.enabled", "true");
        System.setProperty("spring.jpa.show-sql", "false");
    }

    private void configureDevSystem() {
        System.setProperty("elasticsearch.ssl.enabled", "false");
        System.setProperty("spring.jpa.show-sql", "true");
        System.setProperty("springdoc.swagger-ui.enabled", "true");
    }
}
