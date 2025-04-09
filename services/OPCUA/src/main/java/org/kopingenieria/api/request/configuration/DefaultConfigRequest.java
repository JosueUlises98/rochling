package org.kopingenieria.api.request.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultConfigRequest {
    @NotBlank(message = "La configuracion por defecto es obligatoria")
    private DefaultOpcUa defaultConfig;
}
