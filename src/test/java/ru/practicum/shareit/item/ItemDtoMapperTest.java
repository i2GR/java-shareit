package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemDtoMapperTest {

    @Autowired
    private ItemDtoMapper mapper;

    @Test
    void toDto() {
        Item item = Item.builder().id(1L).ownerId(1L).name("item").description("description").available(true).build();

        ItemDto itemDto = mapper.toDto(item);

        assertNotNull(itemDto);
        assertEquals("item", itemDto.getName());
        assertEquals("description", itemDto.getDescription());
        assertEquals(1L, itemDto.getId());
        assertEquals(true, itemDto.getAvailable());
    }

    @Test
    void fromDto() {
        ItemDto itemDto = ItemDto.builder().id(1L).name("item").description("description").available(true).build();

        Item item = mapper.fromDto(itemDto);

        assertNotNull(item);
        assertEquals("item", item.getName());
        assertEquals("description", item.getDescription());
        assertEquals(1L, item.getId());
        assertEquals(true, item.getAvailable());
        assertNull(item.getOwnerId());
    }

    @Test
    void update() {
        Item itemToUpdateName = Item.builder()
                .id(1L)
                .ownerId(1L)
                .name("item")
                .description("description")
                .available(true).build();
        Item itemToUpdateDescription = Item.builder()
                .id(1L)
                .ownerId(1L)
                .name("item")
                .description("description")
                .available(true).build();
        Item itemToUpdateDescriptionAndAvailable = Item.builder()
                .id(1L)
                .ownerId(1L)
                .name("item")
                .description("description")
                .available(true)
                .build();
        Item itemToUpdateNullValues = Item.builder()
                .id(1L)
                .ownerId(1L)
                .name("item")
                .description("description")
                .available(true)
                .build();
        ItemDto itemDtoNameOnly = ItemDto.builder().name("update item").build();
        ItemDto itemDtoDescriptionOnly = ItemDto.builder().description("update description").build();
        ItemDto itemDtoDescriptionAndAvailable = ItemDto.builder()
                .description("update description")
                .available(false)
                .build();
        ItemDto itemDtoNullValues = ItemDto.builder().build();

        mapper.update(itemDtoDescriptionOnly, itemToUpdateDescription);
        mapper.update(itemDtoDescriptionAndAvailable, itemToUpdateDescriptionAndAvailable);
        mapper.update(itemDtoNameOnly, itemToUpdateName);
        mapper.update(itemDtoNullValues, itemToUpdateNullValues);

        assertEquals("update item", itemToUpdateName.getName());
        assertEquals("description", itemToUpdateName.getDescription());
        assertEquals(true, itemToUpdateName.getAvailable());
        assertEquals(1L, itemToUpdateName.getId());
        assertEquals(1L, itemToUpdateName.getOwnerId());

        assertEquals("item", itemToUpdateDescription.getName());
        assertEquals("update description", itemToUpdateDescription.getDescription());
        assertEquals(true, itemToUpdateDescription.getAvailable());
        assertEquals(1L, itemToUpdateDescription.getId());
        assertEquals(1L, itemToUpdateDescription.getOwnerId());

        assertEquals("item", itemToUpdateDescriptionAndAvailable.getName());
        assertEquals("update description", itemToUpdateDescriptionAndAvailable.getDescription());
        assertEquals(false, itemToUpdateDescriptionAndAvailable.getAvailable());
        assertEquals(1L, itemToUpdateDescriptionAndAvailable.getId());
        assertEquals(1L, itemToUpdateDescriptionAndAvailable.getOwnerId());

        assertEquals("item", itemToUpdateNullValues.getName());
        assertEquals("description", itemToUpdateNullValues.getDescription());
        assertEquals(true, itemToUpdateNullValues.getAvailable());
        assertEquals(1L, itemToUpdateNullValues.getId());
        assertEquals(1L, itemToUpdateNullValues.getOwnerId());
    }
}