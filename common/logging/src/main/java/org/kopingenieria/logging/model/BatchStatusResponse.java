package org.kopingenieria.logging.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchStatusResponse {
    private String batchId;
    private String status;
}
