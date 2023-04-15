package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemRequestDtoMapperTest {

    LocalDateTime creationTimeStamp;

    @Autowired
    private ItemRequestDtoMapper mapper;

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

        ItemRequestDto itemRequestDto = mapper.toDto(itemRequest);

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

        ItemRequest itemRequest = mapper.fromDto(itemRequestDto);

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

        mapper.update(itemRequestDtoDescriptionOnly, requestToUpdateDescription);
        mapper.update(itemRequestDtoNewTime, requestToUpdateTime);


        assertEquals("Description", requestToUpdateDescription.getDescription());
        assertEquals(LocalDateTime.MIN, requestToUpdateDescription.getCreated());

        assertEquals("ItemRequest", requestToUpdateTime.getDescription());
        assertEquals(creationTimeStamp, requestToUpdateTime.getCreated());
    }
}