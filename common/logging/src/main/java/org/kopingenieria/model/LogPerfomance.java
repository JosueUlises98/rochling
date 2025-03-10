package org.kopingenieria.model;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogPerfomance {
    String description() default "";
    long threshold() default 1000;
    boolean alertOnThreshold() default true;
    String metricName() default "";
    boolean trackMemory() default false;
    boolean trackCPU() default false;
}
