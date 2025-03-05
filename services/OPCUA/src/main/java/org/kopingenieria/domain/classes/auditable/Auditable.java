package org.kopingenieria.domain.classes.auditable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Auditable {

    @Field("created_by")
    @NotBlank(message = "El usuario creador es requerido")
    private String createdBy;

    @Field("created_by")
    @NotBlank(message = "El usuario creador es requerido")
    private String lastModifiedBy;

    @Field("created_by")
    @NotBlank(message = "El usuario creador es requerido")
    private LocalDateTime createdDate;

    @Field("created_by")
    @NotBlank(message = "El usuario creador es requerido")
    private LocalDateTime lastModifiedDate;
}
