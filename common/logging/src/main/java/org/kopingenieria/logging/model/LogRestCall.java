package org.kopingenieria.logging.model;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogRestCall {
    String event() default "";
    String description() default "";
    boolean includeHeaders() default true;
    boolean includeBody() default true;
    boolean maskSensitiveData() default true;
    String[] sensitiveHeaders() default {"Authorization", "Cookie"};
    boolean logMultipartFiles() default false;
    boolean includeQueryParams() default true;
    LogLevel level() default LogLevel.INFO;
}
