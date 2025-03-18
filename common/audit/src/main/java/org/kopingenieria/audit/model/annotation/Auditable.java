package org.kopingenieria.audit.model.annotation;

import org.kopingenieria.audit.model.AuditEntryType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String value() default "";
    AuditEntryType type();
    boolean includeParams() default true;
    boolean includeResult() default true;
    String description() default "";
    String[]excludeFields() default {};
}
