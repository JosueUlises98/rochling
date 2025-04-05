package org.kopingenieria.api.request.file;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kopingenieria.api.request.configuration.UserConfigRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestFile {

    @NotBlank(message = "El nombre del archivo es obligatorio")
    private String filename;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El estado habilitado/deshabilitado es obligatorio")
    private Boolean enabled;

    @NotNull(message = "La versión es obligatoria")
    private Long version;

    @NotNull(message = "La configuración opcUaConfigRequest es obligatoria")
    private UserConfigRequest opcUaConfigRequest;

}
