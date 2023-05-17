package ru.practicum.shareit.validation;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *  валидация дат начала и конца бронирования <p>
 * начало бронирование строго раньше конца бронирования
 */
public class EndDateAfterStartDateValidator implements ConstraintValidator<EndDateAfterStartDate, BookItemRequestDto> {

    public boolean isValid(BookItemRequestDto dto, ConstraintValidatorContext cxt) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            return false;
        }
        return dto.getStart().isBefore(dto.getEnd());
    }
}