package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoMapperTest {

    private final LocalDateTime START = LocalDateTime.MIN;
    private final LocalDateTime END = LocalDateTime.MAX;

    @Test
    void toDto() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(START).end(END)
                .itemId(1L)
                .bookerId(1L).build();
        //when
        BookingDto bookingDto = BookingDtoMapper.INSTANCE.toDto(booking);
        //then
        assertNotNull(bookingDto);
        assertEquals(START, bookingDto.getStart());
        assertEquals(END, bookingDto.getEnd());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(1L, bookingDto.getBookerId());
    }

    @Test
    void fromDto() {
        BookingDto bookingDto = BookingDto.builder()
                .start(START).end(END)
                .itemId(1L)
                .bookerId(1L).build();
        //when
        Booking booking = BookingDtoMapper.INSTANCE.fromDto(bookingDto);
        //then
        assertNotNull(booking);
        assertEquals(START, booking.getStart());
        assertEquals(END, booking.getEnd());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(1L, bookingDto.getBookerId());
    }

    @Test
    void update() {
        Booking bookingToUpdateStart = Booking.builder()
                .id(1L)
                .start(START).end(END)
                .itemId(1L)
                .bookerId(1L).build();
        Booking bookingToUpdateEnd = Booking.builder()
                .id(1L)
                .start(START).end(END)
                .itemId(1L)
                .bookerId(1L).build();
        Booking bookingToUpdateitemId = Booking.builder()
                .id(1L)
                .start(START).end(END)
                .itemId(1L)
                .bookerId(1L).build();
        LocalDateTime instance = LocalDateTime.now();
        BookingDto dtoUpdateStart = BookingDto.builder().start(instance).build();
        BookingDto dtoUpdateEnd = BookingDto.builder().end(instance).build();
        BookingDto dtoUpdateItemId = BookingDto.builder().itemId(2L).build();
        //when
        BookingDtoMapper.INSTANCE.update(dtoUpdateStart, bookingToUpdateStart);
        BookingDtoMapper.INSTANCE.update(dtoUpdateEnd, bookingToUpdateEnd);
        BookingDtoMapper.INSTANCE.update(dtoUpdateItemId, bookingToUpdateitemId);
        //then
        assertEquals(instance, bookingToUpdateStart.getStart());
        assertEquals(instance, bookingToUpdateEnd.getEnd());
        assertEquals(2L, bookingToUpdateitemId.getItemId());
    }
}