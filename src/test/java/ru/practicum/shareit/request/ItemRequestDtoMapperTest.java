package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .requester(requester)
                .description("description")
                .created(creationTimeStamp).build();

        ItemRequestReplyDto itemRequestDto = mapper.toDto(itemRequest, List.of());

        assertNotNull(itemRequestDto);
        assertEquals("description", itemRequestDto.getDescription());
        assertEquals(creationTimeStamp, itemRequestDto.getCreated());
        assertEquals(1L, itemRequestDto.getId());
    }

    @Test
    void fromDto() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("description")
                .build();

        ItemRequest itemRequest = mapper.fromDto(itemRequestDto, requester, List.of(), creationTimeStamp);

        assertNotNull(itemRequest);
        assertEquals("description", itemRequest.getDescription());
        assertEquals(creationTimeStamp, itemRequest.getCreated());
    }
}