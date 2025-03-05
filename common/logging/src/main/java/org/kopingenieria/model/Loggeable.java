package org.kopingenieria.model;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggeable {
    LogLevel level() default LogLevel.INFO;
    boolean includeParams() default true;
    boolean includeResult() default true;
    boolean includeExecutionTime() default true;
}
