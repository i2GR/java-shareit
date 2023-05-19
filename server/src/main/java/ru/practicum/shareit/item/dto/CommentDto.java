package ru.practicum.shareit.item.dto;

import lombok.*;

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

    private String text;
}