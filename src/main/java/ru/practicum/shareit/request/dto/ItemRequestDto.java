package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.util.IdentifiableDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO для класса ItemRequest <p>
 * ТЗ-13 <p>
 * @implNote expect SP-14 specs for more details
 */
@Data
@Builder
@EqualsAndHashCode(exclude = {"id"})
public class ItemRequestDto implements IdentifiableDto {

    private Long id;

    @NotNull (message = "request description is null")
    @NotBlank(message = "request description name cannot be blank")
    private String description;

    @NotNull (message = "requester is null")
    private Long requesterId;

    private Long responderId;

    @NotNull (message = "creation time is null")
    private LocalDateTime created;
}