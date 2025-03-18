package org.kopingenieria.logging.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchLogResponse {
    private String batchId;
    private int processedCount;
}
