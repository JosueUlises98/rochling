package org.kopingenieria.model;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogDatabase {
    String description() default "";
    String operation() default "";
    boolean includeParams() default true;
    boolean includeQueryTime() default true;
    boolean trackConnections() default true;
    boolean maskSensitiveData() default true;
    String[] sensitiveFields() default {};
}
