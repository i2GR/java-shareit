package ru.practicum.shareit.request.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

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