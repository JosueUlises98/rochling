package org.kopingenieria.domain.classes;

@FunctionalInterface
public interface QualityNetworkListener {
    void onQualityUpdate(QualityNetwork qualityNetwork);
}
