package org.kopingenieria.application.monitoring.quality;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class QualityNetworkObservable {

    private final List<QualityNetworkListener> listeners = new CopyOnWriteArrayList<>();
    private volatile QualityNetwork lastQuality;

    public void addListener(QualityNetworkListener listener) {
        listeners.add(listener);
        // Enviar último valor conocido al nuevo listener
        if (lastQuality != null) {
            listener.onQualityUpdate(lastQuality);
        }
    }

    public void removeListener(QualityNetworkListener listener) {
        listeners.remove(listener);
    }

    public void updateQuality(QualityNetwork quality) {
        this.lastQuality = quality;
        notifyListeners(quality);
    }

    private void notifyListeners(QualityNetwork quality) {
        listeners.forEach(listener -> listener.onQualityUpdate(quality));
    }
}
