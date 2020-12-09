package com.lendico.api.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class FirstDayOfMonthValidator implements ConstraintValidator<FirstDayOfMonth, LocalDateTime> {
    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        return value != null && value.with(TemporalAdjusters.firstDayOfMonth()).equals(value);
    }
}