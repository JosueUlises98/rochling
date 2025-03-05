package org.kopingenieria.config;

@ConfigurationProperties(prefix = "logging.custom")
@Data
public class LoggingProperties {
    private boolean enabled = true;
    private String level = "INFO";
    private boolean includePayload = true;
    private boolean includeHeaders = true;
    private List<String> excludePatterns = new ArrayList<>();
    private int maxPayloadLength = 10000;
}
