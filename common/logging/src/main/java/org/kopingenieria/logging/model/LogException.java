package org.kopingenieria.logging.model;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogException {
    String message() default "Error en la operación";
    LogLevel level() default LogLevel.ERROR;
    Class<? extends Exception>[] exceptions() default {};
    String method() default "";
    String[]stackTrace() default {};
    String component() default "";
}
