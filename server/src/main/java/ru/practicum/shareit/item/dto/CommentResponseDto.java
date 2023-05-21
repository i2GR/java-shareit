package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.time.LocalDateTime;

import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;

/**
 * DTO для класса Comment в http-запросе <p>
 * ТЗ-14
 */
@Getter
@Builder
@EqualsAndHashCode
public class CommentResponseDto {

    private Long id;

    private String text;

    private String authorName;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime created;
}