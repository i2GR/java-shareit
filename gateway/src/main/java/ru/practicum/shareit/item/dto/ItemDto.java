package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

    @NotBlank(message = "item name cannot be blank", groups = {OnCreate.class})
    private String name;

    @NotBlank(message = "item description cannot be blank", groups = {OnCreate.class})
    private String description;

    @NotNull(message = "available cannot be null", groups = {OnCreate.class})
    private Boolean available;

    private Long requestId;
}