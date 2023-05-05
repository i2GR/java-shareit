package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.exception.BadRequestException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BookingStatusTest {

    @ParameterizedTest
    @ValueSource(strings = {"FuTuRe", "current", "PAST", "approVed", "waiting", "ALL"})
    void fromStringWithValidArgs(String str) {

        try {
            BookingStatus status = BookingStatus.fromString(str);
            assertTrue(status instanceof BookingStatus);
            assertTrue(Stream.of(BookingStatus.values()).collect(Collectors.toSet()).contains(status));
        } catch (BadRequestException bre) {
            fail("unexpected exception using string:" + str);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "any"})
    void fromStringWithBadArgs(String str) {
        //then
        assertThrows(BadRequestException.class, () -> BookingStatus.fromString(str));
        assertThrows(BadRequestException.class, () -> BookingStatus.fromString(null));
    }
}