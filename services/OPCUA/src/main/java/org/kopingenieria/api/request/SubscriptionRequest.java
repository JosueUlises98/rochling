package org.kopingenieria.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionRequest {

    @NotNull(message = "El intervalo de publicación es obligatorio")
    @Min(value = 0, message = "El intervalo debe ser mayor o igual a 0")
    private Double publishingInterval;

    @Min(value = 1, message = "El conteo de vida debe ser mayor a 0")
    private Integer lifetimeCount;

    @Min(value = 1, message = "El conteo máximo de keep-alive debe ser mayor a 0")
    private Integer maxKeepAliveCount;

    private Integer maxNotificationsPerPublish;
    private Integer priority;
    private Boolean publishingEnabled;

}
