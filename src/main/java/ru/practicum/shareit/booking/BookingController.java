package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.validation.OnCreate;

import java.util.List;

import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

/**
 * REST-Контроллер запросов на бронирование (Booking) <p>
 * ТЗ-14 <p>
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Размещение запроса на бронирование
     * @param bookerId идентификатор пользователя - заказчика вещи
     * @param dto DTO-класс запроса
     * @return ResponseEntity с DTO-классом запроса и Http-статусом
     */
    @PostMapping
    public BookingResponseDto postBooking(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long bookerId,
                                          @RequestBody @Validated(value = OnCreate.class) BookingDto dto) {
            log.info("[post] booking http-request with booker id {}", bookerId);
            return bookingService.addBooking(bookerId, dto);
    }

    /**
     * Подтверждение запроса на бронирование
     * @param ownerId идентификатор пользователя - владельца вещи
     * @param bookingId идентификатор запроса на бронирование
     * @param approved true/false : бронирование подтверждено/отклонено
     * @return полная доступная информация о бронировании
     */
    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                                            @PathVariable(name = "bookingId") Long bookingId,
                                            @RequestParam(name = "approved") Boolean approved) {
        log.info("[patch] approve Booking http-request of bookingId {} with owner id {}", bookingId, ownerId);
        return bookingService.approve(ownerId, bookingId, approved);
    }

    /** Получение данных о конкретном бронировании (включая его статус) <p>
     * Может быть выполнено либо автором бронирования, либо владельцем вещи, к которой относится бронирование
     * @param userId идентификатор пользователя
     * @param bookingId идентификатор запроса на бронирование
     */
    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId,
                                             @PathVariable(name = "bookingId") Long bookingId) {
        log.info("[get] Booking http-request of bookingId {} with user id {}", bookingId, userId);
        return bookingService.getByRelatedUserId(userId, bookingId);
    }

    /**
     * Получение списка всех бронирований текущего пользователя по статусу бронирования
     * @param bookerId идентификатор пользователя - заказчика вещи
     * @param state строковое представление статуса бронирований
     * @return список данных о бронировании текущего пользователя
     */
    @GetMapping
    public List<BookingResponseDto> getBookingsByBooker(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long bookerId,
                                                        @RequestParam(name = "state", defaultValue = "all") String state) {
        log.info("[get] Booking http-request of bookings of booker id {}", bookerId);
        BookingStatus status = BookingStatus.fromString(state);
        return bookingService.getListByBooker(bookerId, status);
    }

    /**
     * Получение списка бронирований для всех вещей текущего пользователя
     */
    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingByOwner(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                                                      @RequestParam(name = "state", defaultValue = "all") String state) {
        log.info("[get] Booking http-request of bookings of owner id {}", ownerId);
        BookingStatus status = BookingStatus.fromString(state);
        return bookingService.getListByOwner(ownerId, status);
    }
}