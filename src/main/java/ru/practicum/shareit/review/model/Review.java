package ru.practicum.shareit.review.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.util.Identifiable;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Model-класс информации об отзыве <p>
 * ТЗ-13 <p>
 * @implNote expect SP-14 specs for more details
 * @implNote it seems THIS will NOT be necessary
 */
@Data
@Builder
public class Review implements Identifiable {

    private Long id;

    @NotNull(message = "booking id is null")
    private Long bookingId;

    @NotNull(message = "Mark value is null")
    private Integer mark;

    private LocalDateTime created;

    private String message;
}