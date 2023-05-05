package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentDtoMapperTest {

    @Autowired
    private CommentDtoMapper mapper;
    private Item item;
    private User user;

    private LocalDateTime created;

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
        user = User.builder()
                .id(1L)
                .email("user1@host.com")
                .name("user1").build();
        created = LocalDateTime.now();
    }

    @Test
    void toDto() {
        //given
        Comment comment = Comment.builder()
                .id(1L)
                .author(user)
                .item(item)
                .text("comment")
                .created(created)
                .build();
        //when
        CommentResponseDto dto = mapper.toDto(comment);
        //then
        assertEquals(comment.getAuthor().getName(), dto.getAuthorName());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getCreated(), dto.getCreated());}

    @Test
    void fromDto() {
        CommentDto dto = CommentDto.builder()
                .text("comment")
                .build();
        Comment comment = mapper.fromDto(dto, user, item, created);

        assertEquals(comment.getItem().getDescription(), item.getDescription());
        assertEquals(comment.getAuthor().getName(), user.getName());
        assertEquals(comment.getCreated(), created);
        assertNull(comment.getId());
    }

    @Test
    void update() {
        //given
        Comment comment = Comment.builder()
                .id(1L)
                .author(user)
                .item(item)
                .text("comment")
                .created(created)
                .build();
        CommentDto dto = CommentDto.builder().text("updated").build();
        //when
        mapper.update(dto, comment);
        //then
        assertEquals(comment.getItem().getDescription(), item.getDescription());
        assertEquals(comment.getAuthor().getName(), user.getName());
        assertEquals(comment.getCreated(), created);
        assertEquals(1L, comment.getId());
        assertEquals("updated", comment.getText());
    }
}