package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingDtoMapperTest {

    private final LocalDateTime start = LocalDateTime.MIN;
    private final LocalDateTime end = LocalDateTime.MAX;

    @Autowired
    private BookingDtoMapper mapper;

    @Test
    void toDto() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .itemId(1L)
                .bookerId(1L).build();
        //when
        BookingDto bookingDto = mapper.toDto(booking);
        //then
        assertNotNull(bookingDto);
        assertEquals(start, bookingDto.getStart());
        assertEquals(end, bookingDto.getEnd());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(1L, bookingDto.getBookerId());
    }

    @Test
    void fromDto() {
        BookingDto bookingDto = BookingDto.builder()
                .start(start).end(end)
                .itemId(1L)
                .bookerId(1L).build();
        //when
        Booking booking = mapper.fromDto(bookingDto);
        //then
        assertNotNull(booking);
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(1L, bookingDto.getBookerId());
    }

    @Test
    void update() {
        Booking bookingToUpdateStart = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .itemId(1L)
                .bookerId(1L).build();
        Booking bookingToUpdateEnd = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .itemId(1L)
                .bookerId(1L).build();
        Booking bookingToUpdateitemId = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .itemId(1L)
                .bookerId(1L).build();
        LocalDateTime instance = LocalDateTime.now();
        BookingDto dtoUpdateStart = BookingDto.builder().start(instance).build();
        BookingDto dtoUpdateEnd = BookingDto.builder().end(instance).build();
        BookingDto dtoUpdateItemId = BookingDto.builder().itemId(2L).build();
        //when
        mapper.update(dtoUpdateStart, bookingToUpdateStart);
        mapper.update(dtoUpdateEnd, bookingToUpdateEnd);
        mapper.update(dtoUpdateItemId, bookingToUpdateitemId);
        //then
        assertEquals(instance, bookingToUpdateStart.getStart());
        assertEquals(instance, bookingToUpdateEnd.getEnd());
        assertEquals(2L, bookingToUpdateitemId.getItemId());
    }
}