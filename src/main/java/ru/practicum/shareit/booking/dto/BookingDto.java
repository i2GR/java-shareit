package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.util.Entity;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO для класса Booking <p>
 * ТЗ-13 <p>
 * @implNote expect SP-14 specs for more details
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
public class BookingDto extends Entity {

    @Setter
    private Long id;

    @NotNull(message = "booking start time is null")
    private LocalDateTime start;

    @NotNull(message = "booking end time is null")
    private LocalDateTime end;

    private LocalDateTime created;

    /**
     * идентификатор вещи для шаринга - существующий в ShareIt Item#id
     * @implNote передача в заголовке HTTP-запроса. уточнение в следующих спринта
     */
    private Long itemId;

    /**
     * идентификатор пользователя-заказчика - существующий в ShareIt User#id
     * @implNote передача в заголовке HTTP-запроса. уточнение в следующих спринтах
     */
    private Long bookerId;

    @Builder.Default
    private BookingStatus status = BookingStatus.WAITING;
}