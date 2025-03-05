package org.kopingenieria.config;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfig {

    @Bean
    public LoggingService loggingService() {
        return new LoggingService();
    }

    @Bean
    public LoggingAspect loggingAspect(LoggingService loggingService) {
        return new LoggingAspect(loggingService);
    }

    @Bean
    public RestControllerLoggingAspect restControllerLoggingAspect(LoggingService loggingService) {
        return new RestControllerLoggingAspect(loggingService);
    }
}
