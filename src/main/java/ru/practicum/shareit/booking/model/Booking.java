package ru.practicum.shareit.booking.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.util.Entity;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Model-класс информации о заспросе вещи <p>
 * ТЗ-13 <p>
 */

@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = false)
public class Booking extends Entity {

    private Long id;

    @NotNull(message = "booking start time is null")
    private LocalDateTime start;

    @NotNull(message = "booking end time is null")
    private LocalDateTime end;
    /**
     * создание заказа
     */
    private LocalDateTime created;

    /**
     * идентификатор вещи для шаринга - существующий в ShareIt Item#id
     */
    @NotNull(message = "booking item is null")
    private Long itemId;

    /**
     * идентификатор пользователя-заказчика - существующий в ShareIt User#id
     */
    @NotNull(message = "booker is null")
    private Long bookerId;

    /**
     * можно предположить статус может меняться во время работы приложения
     */
    @Builder.Default
    BookingStatus status = BookingStatus.WAITING;
}