package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.util.Entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO для класса ItemRequest <p>
 * ТЗ-13 <p>
 * @implNote expect SP-14 specs for more details
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
public class ItemRequestDto extends Entity {

    @Setter
    private Long id;

    @NotNull (message = "request description is null")
    @NotBlank(message = "request description name cannot be blank")
    private String description;

    @NotNull (message = "requester is null")
    private Long requesterId;

    private Long responderId;

    @NotNull (message = "creation time is null")
    private LocalDateTime created;
}