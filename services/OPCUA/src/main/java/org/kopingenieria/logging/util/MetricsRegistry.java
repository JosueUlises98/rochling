package org.kopingenieria.logging.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MetricsRegistry {

    private final MeterRegistry registry;

    public MetricsRegistry(MeterRegistry registry) {
        this.registry = registry;
    }
    
    public void incrementRequestCounter(String className, String methodName, boolean success) {
        Counter counter = registry.counter("request_total", "class", className, "method", methodName, "result", success ? "success" : "failure");
        counter.increment();
    }
    
    public void decrementRequestCounter(String className, String methodName, boolean success) {
        Counter counter = registry.counter("request_total", "class", className, "method", methodName, "result", success ? "success" : "failure");
        counter.increment(-1.0);
    }
    
    public double getCounterValue(String counterName, String... tags) {
        Counter counter = registry.find(counterName).tags(tags).counter();
        return (counter != null) ? counter.count() : 0.0;
    }
    
    public void recordRequestDuration(String className, String methodName, long duration) {
        DistributionSummary summary = registry.summary("request_duration_ms", "class", className, "method", methodName);
        summary.record(duration);
    }
    
    public DistributionSummary getDistributionSummary(String summaryName, String... tags) {
        return registry.find(summaryName).tags(tags).summary();
    }
    
    public void resetDistributionSummary(String summaryName, String... tags) {
        DistributionSummary summary = registry.find(summaryName).tags(tags).summary();
        if (summary != null) {
            // Simulating a reset by re-registering - actual reset depends on the metric tool's implementation
            HistogramSnapshot snapshot = summary.takeSnapshot();
            registry.gauge(summaryName + "_count", snapshot.count());
            registry.gauge(summaryName + "_total", snapshot.total());
            registry.gauge(summaryName + "_max", snapshot.max());
            registry.gauge(summaryName + "_mean", snapshot.mean());
        }
    }
    
    public void registerCustomGauge(String gaugeName, Number value, String... tags) {
        registry.gauge(gaugeName, Arrays.stream(tags).map(tag -> {
            String[] parts = tag.split(":", 2); // Split into key-value pairs
            if (parts.length == 2) {
                return Tag.of(parts[0], parts[1]);
            } else {
                // Handle invalid tag format (e.g., log a warning and return a dummy tag or throw an exception)
                return Tag.of("invalid_tag", tag);
            }
        }).collect(Collectors.toList()), value, Number::doubleValue);
    }
}
