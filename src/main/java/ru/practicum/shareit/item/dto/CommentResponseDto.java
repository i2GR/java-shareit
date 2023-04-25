package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * DTO для класса Comment в http-запросе <p>
 * ТЗ-14
 */
@Getter
@Builder
public class CommentResponseDto {

    private Long id;

    private String text;

    private String authorName;

    private LocalDateTime created;
}