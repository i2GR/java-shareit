package ru.practicum.shareit.booking.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.now();
    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private User booker = User.builder().name("booker").email("booker@host.dom").build();
    private Item item = Item.builder().id(1L).ownerId(1L).name("item").description("description").available(true).build();

    @Test
    void testEquals() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking bookingCopy = Booking.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking bookingNoId = Booking.builder()
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking bookingAnotherStatus = Booking.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        assertEquals(booking, bookingCopy);
        assertEquals(booking, bookingNoId);
        assertNotEquals(booking, bookingAnotherStatus);
    }
}