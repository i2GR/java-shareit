package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.util.Entity;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Model-класс информации о заспросе вещи <p>
 * ТЗ-13 <p>
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class ItemRequest extends Entity {

    private Long id;

    @NotNull (message = "request description is null")
    @NotBlank(message = "item request description cannot be blank")
    private String description;

    /**
     * идентификатор запросившего пользователия - существующий в ShareIt User#id
     */
    @NotNull (message = "requester is null")
    private Long requesterId;

    /**
     * идентификатор владельца ,откликнувшегося на запрос - существующий в ShareIt User#id
     */
    private Long responderId;

    @NotNull (message = "creation time is null")
    private LocalDateTime created;
}