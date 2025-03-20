package org.kopingenieria.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionResponse {
    private Long id;
    private Double publishingInterval;
    private Integer lifetimeCount;
    private Integer maxKeepAliveCount;
    private Integer maxNotificationsPerPublish;
    private Integer priority;
    private Boolean publishingEnabled;
    private LocalDateTime createdAt;
}
