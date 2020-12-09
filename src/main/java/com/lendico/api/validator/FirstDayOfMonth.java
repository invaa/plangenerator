package com.lendico.api.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = FirstDayOfMonthValidator.class)
public @interface FirstDayOfMonth {
    String message() default "Date should be first day of the month";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
