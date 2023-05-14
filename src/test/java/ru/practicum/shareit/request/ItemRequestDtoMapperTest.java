package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestReplyDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemRequestDtoMapperTest {

    LocalDateTime creationTimeStamp;

    @Autowired
    private ItemRequestDtoMapper mapper;

    private final User requester = User.builder().id(1L).name("requester").email("requester@host.dom").build();

    @BeforeEach
    void instanceTime() {
        creationTimeStamp = LocalDateTime.now();
    }

    @Test
    void toDto() {
        //given
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .requester(requester)
                .description("description")
                .created(creationTimeStamp).build();
        //when
        ItemRequestReplyDto itemRequestDto = mapper.toDto(itemRequest, List.of());
        //then
        assertNotNull(itemRequestDto);
        assertEquals("description", itemRequestDto.getDescription());
        assertEquals(creationTimeStamp, itemRequestDto.getCreated());
        assertEquals(1L, itemRequestDto.getId());
    }

    @Test
    void fromDto() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("description")
                .build();

        ItemRequest itemRequest = mapper.fromDto(itemRequestDto, requester, List.of(), creationTimeStamp);

        assertNotNull(itemRequest);
        assertEquals("description", itemRequest.getDescription());
        assertEquals(creationTimeStamp, itemRequest.getCreated());
        assertNull(itemRequest.getId());
    }


    @Test
    void map() {
        //given
        Item item = Item.builder()
                .id(1L)
                .ownerId(1L)
                .name("item")
                .description("description")
                .available(true)
                .request(ItemRequest.builder().id(1L).build())
                .build();
        //when
        ItemRequestReplyDto.ItemDto dto = mapper.map(item);
        //then
        assertNull(mapper.map(null));
        assertNotNull(dto);
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getOwnerId(), dto.getOwnerId());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
        assertEquals(item.getRequest().getId(), dto.getRequestId());
    }
}