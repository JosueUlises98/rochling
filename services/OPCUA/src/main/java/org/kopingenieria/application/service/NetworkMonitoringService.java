package org.kopingenieria.application.service;

public class NetworkMonitoringService {
    private final QualityNetworkObservable observable;

    public NetworkMonitoringService(QualityNetworkObservable observable) {
        this.observable = observable;
        initializeMonitoring();
    }

    private void initializeMonitoring() {
        // 1. Observer para logging
        observable.addListener(new LoggingListener());

        // 2. Observer para dashboard
        observable.addListener(new DashboardListener());

        // 3. Observer para alertas
        observable.addListener(new AlertListener());

        // 4. Observer con lambda
        observable.addListener(quality ->
                System.out.println("Nueva calidad: " + quality));
    }

    public void checkNetworkQuality(){
        QualityNetwork qualityNetwork =
        observable.updateQuality();
    }
}
