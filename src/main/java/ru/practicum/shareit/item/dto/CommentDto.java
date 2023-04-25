package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
public class CommentDto {

    @NotBlank(message = "comment name cannot be blank", groups = {OnCreate.class})
    private String text;
}