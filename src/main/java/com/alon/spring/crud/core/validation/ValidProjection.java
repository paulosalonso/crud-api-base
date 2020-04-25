package com.alon.spring.crud.core.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidaProjectionValidator.class)
public @interface ValidProjection {
    String message() default "projection not found";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] value() default {};
}
