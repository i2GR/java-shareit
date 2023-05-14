package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.validation.OnCreate;

import javax.validation.constraints.NotBlank;

/**
 * DTO для класса Comment d http-запросе <p>
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