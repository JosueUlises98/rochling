package org.kopingenieria.model;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditableOperation {
    String value() default "";
    AuditEntryType type();
    boolean includeParams() default true;
    boolean includeResult() default true;
    String description() default "";
}
