package ru.practicum.shareit.booking.validation;

import ru.practicum.shareit.booking.dto.BookingDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * реализация аннтоции для проверки дат начала и конца бронирования <p>
 * начало бронирование строго раньше конца бронирования
 */
public class EndDateAfterStartDateValidator implements ConstraintValidator<EndDateAfterStartDate, BookingDto> {

    public boolean isValid(BookingDto dto, ConstraintValidatorContext cxt) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            return false;
        }
        return dto.getStart().isBefore(dto.getEnd());
    }
}