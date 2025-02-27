package org.kopingenieria.model.enums.network.connection;

import java.util.concurrent.TimeUnit;

public enum Timeouts {

    CONNECTION(30, TimeUnit.SECONDS),
    SESSION(15, TimeUnit.MINUTES),
    RECONNECTION(10, TimeUnit.SECONDS),
    DISCONNECTION(5, TimeUnit.SECONDS);

    private final Integer duration;
    private final TimeUnit unit;

    Timeouts(Integer duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    public Integer getDuration() {
        return duration;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public long toMilliseconds() {
        return unit.toMillis(duration);
    }

    public long toSeconds() {
        return unit.toSeconds(duration);
    }

    public long toMinutes() {
        return unit.toMinutes(duration);
    }

    public long toHours() {
        return unit.toHours(duration);
    }
    
}
