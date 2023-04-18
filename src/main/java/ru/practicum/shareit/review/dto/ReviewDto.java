package ru.practicum.shareit.review.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.util.ShareItEntity;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**К
 * DTO для класса Review <p>
 * ТЗ-13 <p>
 * @implNote expect SP-14 specs for more details
 * @implNote it seems THIS will NOT be necessary
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
public class ReviewDto extends ShareItEntity {

    @Setter
    private Long id;

    @NotNull(message = "booking id is null")
    private Long bookingId;

    @NotNull(message = "Mark value is null")
    @Min(value = 0, message = "Mark value out of minimum range")
    @Max(value = 5, message = "Mark value out of maximum range")
    private Integer mark;

    private String message;
}