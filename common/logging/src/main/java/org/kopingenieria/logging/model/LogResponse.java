package org.kopingenieria.logging.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogResponse {
    private String id;
    private String status;
}
