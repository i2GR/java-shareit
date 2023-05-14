package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.practicum.shareit.booking.validation.OnCreate;

import javax.validation.constraints.NotBlank;

/**
 * DTO для класса ItemRequest <p>
 * ТЗ-13 <p>
 * @implNote expect SP-15 specs for more details
 */
@Getter
@Builder
@EqualsAndHashCode(exclude = {"id"})
public class ItemRequestDto {

    private Long id;

    @NotBlank(groups = {OnCreate.class},
            message = "request description name cannot be blank")
    private String description;
}