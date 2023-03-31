package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestDtoMapperTest {

    LocalDateTime creationTimeStamp;

    @BeforeEach
    void instanceTime() {
        creationTimeStamp = LocalDateTime.now();
    }

    @Test
    void toDto() {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L).requesterId(1L)
                .description("description")
                .created(creationTimeStamp)
                .created(creationTimeStamp).build();

        ItemRequestDto itemRequestDto = ItemRequestDtoMapper.INSTANCE.toDto(itemRequest);

        assertNotNull(itemRequestDto);
        assertEquals("description", itemRequestDto.getDescription());
        assertEquals(1L, itemRequestDto.getId());
        assertEquals(creationTimeStamp, itemRequestDto.getCreated());
    }

    @Test
    void fromDto() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("description")
                .created(creationTimeStamp).build();

        ItemRequest itemRequest = ItemRequestDtoMapper.INSTANCE.fromDto(itemRequestDto);

        assertNotNull(itemRequest);
        assertEquals("description", itemRequest.getDescription());
        assertEquals(1L, itemRequest.getId());
        assertEquals(creationTimeStamp, itemRequest.getCreated());
    }

    @Test
    void update() {
        ItemRequest requestToUpdateDescription = ItemRequest.builder().id(1L)
                .description("ItemRequest")
                .created(LocalDateTime.MIN).build();
        ItemRequest requestToUpdateTime = ItemRequest.builder().id(1L)
                .description("ItemRequest")
                .created(LocalDateTime.MIN).build();

        ItemRequestDto itemRequestDtoDescriptionOnly =  ItemRequestDto.builder().description("Description").build();
        ItemRequestDto itemRequestDtoNewTime = ItemRequestDto.builder().created(creationTimeStamp).build();

        ItemRequestDtoMapper.INSTANCE.update(itemRequestDtoDescriptionOnly, requestToUpdateDescription);
        ItemRequestDtoMapper.INSTANCE.update(itemRequestDtoNewTime, requestToUpdateTime);


        assertEquals("Description", requestToUpdateDescription.getDescription());
        assertEquals(LocalDateTime.MIN, requestToUpdateDescription.getCreated());

        assertEquals("ItemRequest", requestToUpdateTime.getDescription());
        assertEquals(creationTimeStamp, requestToUpdateTime.getCreated());
    }
}