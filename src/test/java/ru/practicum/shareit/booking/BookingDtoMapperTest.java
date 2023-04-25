package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingDtoMapperTest {

    private final LocalDateTime start = LocalDateTime.MIN;
    private final LocalDateTime end = LocalDateTime.MAX;

    private User booker = User.builder().id(1L).name("owner").email("owner@host.dom").build();

    private final Item item = Item.builder()
            .id(1L)
            .ownerId(1L)
            .name("item")
            .description("description")
            .available(true)
            .build();

    @Autowired
    private BookingDtoMapper mapper;

    @Test
    void toDto() {
        Booking booking = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .item(item)
                .booker(booker)
                .build();
        //when
        BookingResponseDto bookingResponseDto = mapper.toDto(booking);
        //then
        assertNotNull(bookingResponseDto);
        assertEquals(start, bookingResponseDto.getStart());
        assertEquals(end,bookingResponseDto.getEnd());
        assertEquals(1L, bookingResponseDto.getItem().getId());
        assertEquals(1L, bookingResponseDto.getBooker().getId());
    }

    @Test
    void fromDto() {
        BookingDto bookingDto = BookingDto.builder()
                .start(start).end(end)
                .itemId(1L)
                .build();
        //when
        Booking booking = mapper.fromDto(bookingDto, booker, item);
        //then
        assertNotNull(booking);
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }

    @Test
    void update() {
        Booking bookingToUpdateStart = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .item(item)
                .build();
        Booking bookingToUpdateEnd = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .item(item)
                .build();
        Booking bookingToUpdateItemId = Booking.builder()
                .id(1L)
                .start(start).end(end)
                .item(item)
                .build();
        LocalDateTime instance = LocalDateTime.now();
        BookingDto dtoUpdateStart = BookingDto.builder().start(instance).build();
        BookingDto dtoUpdateEnd = BookingDto.builder().end(instance).build();
        BookingDto dtoUpdateItemId = BookingDto.builder().itemId(2L).build();
        //when
        mapper.update(dtoUpdateStart, bookingToUpdateStart);
        mapper.update(dtoUpdateEnd, bookingToUpdateEnd);
        mapper.update(dtoUpdateItemId, bookingToUpdateItemId);
        //then
        assertEquals(instance, bookingToUpdateStart.getStart());
        assertEquals(instance, bookingToUpdateEnd.getEnd());
        assertEquals(1L, bookingToUpdateItemId.getItem().getId());
    }
}