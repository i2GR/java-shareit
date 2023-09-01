package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;

/**
 * DTO для класса сущности запроса на вещь ItemRequest <p>
 * ТЗ-15 <p>
 * DTO, направляемый в ответ на HTTP-запрос
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
public class ItemRequestReplyDto {

    @Setter
    private Long id;

    private String description;

    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime created;

    private List<ItemDto> items;

    @Getter
    @Builder
    @EqualsAndHashCode
    public static class ItemDto {
        private Long id;
        private String name;
        private Long ownerId;
        private String description;
        private Long requestId;
        private Boolean available;
    }
}