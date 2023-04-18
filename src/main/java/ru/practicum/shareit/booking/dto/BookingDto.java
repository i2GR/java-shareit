package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

import ru.practicum.shareit.booking.validation.EndDateAfterStartDate;

/**
 * DTO для класса сущности запроса на бронирование
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@EndDateAfterStartDate
public class BookingDto {

    @Setter
    private Long id;

    @FutureOrPresent
    private LocalDateTime start;

    @Future
    private LocalDateTime end;

    /**
     * идентификатор вещи для шаринга - существующий в ShareIt Item#id
     * @implNote передача в заголовке HTTP-запроса. уточнение в следующих спринта
     */
    private Long itemId;
}