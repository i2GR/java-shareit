package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.util.Entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO для класса Item <p>
 * ТЗ-13
 */
@Getter
@Builder
public class ItemDto extends Entity {

    @Setter
    private Long id;

    @NotNull(message = "name cannot be null")
    @NotBlank(message = "item name cannot be blank")
    private String name;

    @NotBlank(message = "item description cannot be blank")
    private String description;

    @NotNull(message = "available cannot be null")
    private Boolean available;
}