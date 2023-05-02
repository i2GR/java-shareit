package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для класса Item для возврата в качестве тела ответа Http-запроса<p>
 * содержит дополнительные поля с информацией о запрсоах на бронирование, комментариях
 * ТЗ-14
 */
@Getter
@Builder
public class ItemResponseDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingDto lastBooking;

    private BookingDto nextBooking;

    @Setter
    private List<CommentResponseDto> comments;

    private Long requestId;

    @Builder
    @Getter
    public static class BookingDto {
        Long id;
        LocalDateTime start;
        LocalDateTime end;
        Long bookerId;
    }
}