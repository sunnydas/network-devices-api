package org.sunny.deviceapi.models.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MacAddressValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMacAddress {
    String message() default "Invalid MAC address format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
