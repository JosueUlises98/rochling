package org.kopingenieria.logging.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.kopingenieria.logging.aspect.LoggingAspect;
import org.kopingenieria.logging.service.LoggingServiceImpl;
import org.kopingenieria.logging.service.NotificationServiceImpl;
import org.kopingenieria.logging.util.MetricsRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@EnableAspectJAutoProxy
public class LoggingConfig {

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect(new LoggingServiceImpl(new MetricsRegistry(new SimpleMeterRegistry()),new NotificationServiceImpl(new JavaMailSenderImpl())));
    }

    @Bean
    public MetricsRegistry metricsRegistry() {
        return new MetricsRegistry(new SimpleMeterRegistry());
    }

    @Bean
    public NotificationServiceImpl notificationService() {
        return new NotificationServiceImpl(new JavaMailSenderImpl());
    }
}
