package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import ru.practicum.shareit.booking.model.BookingStatus;

import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;

/**
 * DTO для класса сущности запроса на бронирование
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
public class BookingResponseDto {

    @Setter
    private Long id;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime start;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime end;

    private ItemDto item;

    private BookerDto booker;

    private BookingStatus status;

    @Getter
    @Builder
    @EqualsAndHashCode
    public static class BookerDto {
        private long id;
        private String name;
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    public static class ItemDto {
        private long id;
        private String name;
    }
}