package org.kopingenieria.domain.classes;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.kopingenieria.domain.enums.client.network.connection.QualityLevel;
import java.time.LocalDateTime;

@Value
@Builder
public class QualityNetwork{

    double latency;
    double jitter;
    double packetLoss;
    double bandwidth;
    LocalDateTime timestamp;  // ISO-8601 format
    NetworksMetrics metrics;

    @Data
    public static class NetworksMetrics {
        private Long goodCount;
        private Long badCount;
        private Long dataChangeCount;
        private Long eventCount;
        private Double averagePublishTime;
        private Double averageDataChangeTime;
        private Long totalBytesReceived;
        private Long totalBytesSent;
    }

    @Override
    public String toString() {
        return "QualityConnection{" +
                "latency=" + latency +
                ", jitter=" + jitter +
                ", packetLoss=" + packetLoss +
                ", bandwidth=" + bandwidth +
                ", timestamp='" + timestamp + '\'' +
                ", metrics=" + metrics +
                '}';
    }

    public QualityLevel getQualityLevel() {
        if (latency < 50 && jitter < 10 && packetLoss < 0.1) {
            return QualityLevel.EXCELLENT;
        } else if (latency < 100 && jitter < 20 && packetLoss < 0.5) {
            return QualityLevel.GOOD;
        } else if (latency < 200 && jitter < 30 && packetLoss < 1.0) {
            return QualityLevel.FAIR;
        }
        return QualityLevel.POOR;
    }
}
