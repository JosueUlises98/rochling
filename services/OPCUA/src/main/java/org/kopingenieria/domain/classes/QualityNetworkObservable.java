package org.kopingenieria.domain.classes;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QualityNetworkObservable {

    private static final Logger log = LoggerFactory.getLogger(QualityNetworkObservable.class);

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
        listeners.forEach(listener -> {
            try {
                listener.onQualityUpdate(quality);
            } catch (Exception e) {
                log.error("Error notificando al listener", e);
            }
        });
    }
}
