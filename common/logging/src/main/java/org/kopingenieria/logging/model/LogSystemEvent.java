package org.kopingenieria.logging.model;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogSystemEvent {
    String description() default "";
    String metricName() default "";
    String event();
    LogLevel level() default LogLevel.INFO;
}
