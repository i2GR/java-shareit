package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


/**
 * DTO для класса сущности запроса на бронирование
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BookingDto {

    @Setter
    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Long itemId;
}