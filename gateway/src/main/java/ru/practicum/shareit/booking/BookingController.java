package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

/**
 * REST-Контроллер бронирований пользователей (Booking) <p>
 * методы шлюза. Описание и назначение - по методам Сервера (модуль shareit-server) <p>
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader(SHARER_USER_HTTP_HEADER) Long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Long from,
			@RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {

		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	/**
	 * Получение списка бронирований для всех вещей текущего пользователя
	 */
	@GetMapping("/owner")
	public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
											@RequestParam(name = "state", defaultValue = "all") String state,
											@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Long from,
											@RequestParam(name = "size", defaultValue = "20") @Positive Integer size) {

		log.info("[get] Booking http-request of bookings of owner id {}", ownerId);
		BookingState status = BookingState.from(state)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
		return bookingClient.getBookingsByOwner(ownerId, status, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> bookItem(@RequestHeader(SHARER_USER_HTTP_HEADER) Long userId,
										   @RequestBody @Validated(value = OnCreate.class) BookItemRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.bookItem(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader(SHARER_USER_HTTP_HEADER) Long userId,
			@PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approveBooking(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
											 @PathVariable(name = "bookingId") Long bookingId,
											 @RequestParam(name = "approved") Boolean approved) {
		log.info("[patch] approve Booking http-request of bookingId {} with owner id {}", bookingId, ownerId);
		return bookingClient.approve(ownerId, bookingId, approved);
	}

}