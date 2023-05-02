package ru.practicum.shareit.booking.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннтоция для проверки дат начала и конца бронирования
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = EndDateAfterStartDateValidator.class)
public @interface EndDateAfterStartDate {
    String message() default "Booking endDate before booking startDate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
