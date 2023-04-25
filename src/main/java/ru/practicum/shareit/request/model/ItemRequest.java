package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
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
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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