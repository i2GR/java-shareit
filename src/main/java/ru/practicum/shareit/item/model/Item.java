package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.util.Entity;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Model-класс информации о вещи для шаринга <p>
 * ТЗ-13
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"id"}, callSuper = false)
public class Item extends Entity {

    private Long id;
    /**
     * идентификатор владельца - User#id
     */
    @NotNull (message = "Owner is null")
    private Long ownerId;

    @NotNull (message = "item name is null")
    private String name;

    @NotBlank(message = "item description cannot be blank")
    private String description;

    @NotNull (message = "item available is null")
    private Boolean available;

    private ItemRequest request;
}