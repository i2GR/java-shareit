package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.validation.OnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO для класса ItemRequest <p>
 * ТЗ-13 <p>
 * @implNote expect SP-15 specs for more details
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
public class ItemRequestDto {

    //@Setter
    private Long id;

    @NotBlank(groups = {OnCreate.class},
            message = "request description name cannot be blank")
    private String description;

    //@NotNull (message = "requester is null")
    //private Long requesterId;

    //@NotNull (message = "creation time is null")
    //private LocalDateTime created;
}