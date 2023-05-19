package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.*;
import ru.practicum.shareit.validation.EndDateAfterStartDate;
import ru.practicum.shareit.validation.OnCreate;

/**
 * DTO для класса сущности запроса на бронирование
 */
@Builder
@Getter
//@AllArgsConstructor
//@NoArgsConstructor
@EndDateAfterStartDate(groups = {OnCreate.class})
@EqualsAndHashCode(exclude = {"id"})
public class BookItemRequestDto {

	@Setter
	private long id;

	@NotNull(groups = {OnCreate.class})
	private Long itemId;

	@FutureOrPresent(groups = {OnCreate.class})
	private LocalDateTime start;

	@Future(groups = {OnCreate.class})
	private LocalDateTime end;
}
