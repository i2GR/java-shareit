package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemResponseDtoMapperTest {

    private Item item;

    @BeforeEach
    void setup() {
        item = Item.builder()
                .id(1L)
                .ownerId(1L)
                .name("item")
                .description("description")
                .available(true)
                .request(ItemRequest.builder().id(1L).build())
                .build();
    }


    @Autowired
    private ItemResponseDtoMapper mapper;

    @Test
    void toDto() {
        //when
        ItemResponseDto itemDto = mapper.toDto(item);
        //then
        assertNotNull(itemDto);
        assertEquals("item", itemDto.getName());
        assertEquals("description", itemDto.getDescription());
        assertEquals(1L, itemDto.getId());
        assertEquals(true, itemDto.getAvailable());
        assertEquals(1L, itemDto.getRequestId());
    }

    @Test
    void map() {
        //given
        User booker = User.builder().id(1L).name("owner").email("owner@host.dom").build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.MIN)
                .end(LocalDateTime.MAX)
                .item(item)
                .booker(booker)
                .build();
        //when
        ItemResponseDto.BookingDto bookingDto = mapper.map(booking);
        //then
        assertNull(mapper.map(null));
        assertNotNull(bookingDto);
        assertEquals(LocalDateTime.MIN, bookingDto.getStart());
        assertEquals(LocalDateTime.MAX, bookingDto.getEnd());
        assertEquals(1L, bookingDto.getId());
        assertEquals(1L, bookingDto.getBookerId());
    }
}