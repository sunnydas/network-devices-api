package org.sunny.deviceapi.models.dto.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MacAddressValidator implements ConstraintValidator<ValidMacAddress, String> {

    private static final String MAC_ADDRESS_REGEX = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value.matches(MAC_ADDRESS_REGEX);
    }
}