package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для класса Item <p>
 * ТЗ-13
 * используется также для возврата для http после добавления вещи
 * <b>не</b>содержит дополнительные поля с информацией о запросах на бронирование, комментариях
 * ТЗ-15 поле requestId - id запроса, в ответ на который создаётся нужная вещь
 */
@Builder
@Getter
@EqualsAndHashCode(exclude = {"id"})
public class ItemDto {

    @Setter
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;
}