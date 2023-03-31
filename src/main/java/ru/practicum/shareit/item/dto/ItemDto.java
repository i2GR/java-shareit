package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.util.IdentifiableDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO для класса Item <p>
 * ТЗ-13
 */
@Data
@Builder
public class ItemDto implements IdentifiableDto {
    private Long id;

    @NotNull(message = "name cannot be null")
    @NotBlank(message = "item name cannot be blank")
    private String name;

    @NotBlank(message = "item description cannot be blank")
    private String description;

    @NotNull(message = "available cannot be null")
    private Boolean available;
}