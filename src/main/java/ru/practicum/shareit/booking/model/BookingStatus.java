package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.exception.BadRequestException;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum BookingStatus {
    /**
     * новое бронирование, ожидает одобрения
     */
    WAITING,

    /**
     * бронирование подтверждено владельцем
     */
    APPROVED,

    /**
     * бронирование отклонено владельцем
     */
    REJECTED,

    /**
     * бронирование отменено создателем
     */
    CANCELED,

    /**
     * полный список бронирований (параметр запроса)
     */
    ALL,

    /**
     * списка завершенных бронирований (параметр запроса)
     */
    PAST,

    /**
     *  список [текущих] бронирований (параметр запроса)
     */
    CURRENT,

    /**
     *  список [будущих] бронирований (параметр запроса)
     */
    FUTURE;

    private static final Set<String> statuses = Stream.of(BookingStatus.values())
            .map(BookingStatus::toString)
            .collect(Collectors.toSet());

    public static BookingStatus fromString(String str) {
        if (statuses.contains(str.toUpperCase())) {
            return BookingStatus.valueOf(BookingStatus.class, str.toUpperCase());
        }
        throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
    }

}