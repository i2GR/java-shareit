package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.NotBlank;

/**
 * DTO для класса ItemRequest <p>
 * ТЗ-16 <p>
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


