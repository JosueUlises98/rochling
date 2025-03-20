package org.kopingenieria.application.monitoring.quality;

@FunctionalInterface
public interface QualityNetworkListener {
    void onQualityUpdate(QualityNetwork qualityNetwork);
}
