package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO для класса Comment d http-запросе <p>
 * ТЗ-14
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    @NotNull(message = "comment cannot be null")
    @NotBlank(message = "comment name cannot be blank")
    private String text;
}