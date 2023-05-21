package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

import ru.practicum.shareit.validation.OnCreate;

/**
 * DTO для класса Comment http-запроса <p>
 * ТЗ-14
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CommentDto {

    @NotBlank(message = "comment name cannot be blank", groups = {OnCreate.class})
    private String text;
}