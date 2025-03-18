package org.kopingenieria.logging.model;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogMethod {
    String description() default "";
    String operation() default "";
    LogLevel level() default LogLevel.INFO;
    boolean includeArgs() default true;
    boolean includeResult() default true;
    boolean includeExecutionTime() default true;
    boolean maskSensitiveData() default true;
    String[] sensitiveFields() default {};
}
