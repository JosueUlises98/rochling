package org.kopingenieria.api.request.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kopingenieria.domain.model.user.UserOpcUa;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfigRequest {
    @NotBlank(message = "La configuracion del usuario es obligatoria")
    private UserOpcUa userConfig;
}
