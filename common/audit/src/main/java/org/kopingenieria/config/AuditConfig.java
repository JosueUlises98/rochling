package org.kopingenieria.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
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
public class AuditConfig {
    
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();
    private RetentionConfig retention = new RetentionConfig();
    private CacheConfig cache = new CacheConfig();
    private boolean asyncEnabled = true;

    @Data
    public static class ThreadPoolConfig {
        private int coreSize = 2;
        private int maxSize = 4;
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
}
