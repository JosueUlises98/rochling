package org.kopingenieria.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.model.LogLevel;
import org.kopingenieria.model.LogSystemEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableJpaAuditing
@EnableCaching
@ConfigurationProperties(prefix = "audit")
@Data
@Slf4j
public class AuditConfig {
    
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();
    private RetentionConfig retention = new RetentionConfig();
    private CacheConfig cache = new CacheConfig();
    private boolean asyncEnabled = true;
    private static final String APP_NAME = "audit";
    private static final String APP_VERSION = "1.0.0";
    private static final String APP_DESCRIPTION = "Audit Service";

    @Data
    @Validated
    public static class ThreadPoolConfig {
        @Min(1) @Max(10)
        private int coreSize = 2;
        @Min(1)
        private int maxSize = 4;
        @Min(1)
        private int queueCapacity = 100;
        private int keepAliveSeconds = 60;
        private String threadNamePrefix = "Audit-";
        private boolean allowCoreThreadTimeout = true;
        private boolean waitForTasksToCompleteOnShutdown = true;
        private int awaitTerminationSeconds = 60;
    }

    @Data
    public static class RetentionConfig {
        private int days = 90;
        private boolean enabled = true;
        private String cleanupCron = "0 0 1 * * ?";
    }

    @Data
    public static class CacheConfig {
        private boolean enabled = true;
        private String cacheName = "auditEvents";
        private int timeToLiveSeconds = 3600;
        private int maxElements = 1000;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(cache.getCacheName());
    }

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPool.getCoreSize());
        executor.setMaxPoolSize(threadPool.getMaxSize());
        executor.setQueueCapacity(threadPool.getQueueCapacity());
        executor.setThreadNamePrefix(threadPool.getThreadNamePrefix());
        executor.setKeepAliveSeconds(threadPool.getKeepAliveSeconds());
        executor.setAllowCoreThreadTimeOut(threadPool.isAllowCoreThreadTimeout());
        executor.setWaitForTasksToCompleteOnShutdown(threadPool.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(threadPool.getAwaitTerminationSeconds());
        executor.setRejectedExecutionHandler((r, e) -> {
            throw new RuntimeException("Cola de auditoría llena - No se pueden procesar más eventos");
        });
        executor.initialize();
        return executor;
    }

    @PostConstruct
    @LogSystemEvent(event = "Load Configuration",description = "Loading audit configuration",level = LogLevel.INFO)
    public void logConfig() {
        log.info("Configurando AuditService");
    }
}
