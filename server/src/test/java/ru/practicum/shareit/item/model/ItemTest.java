package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testEquals() {
        Item item1 = Item.builder().id(1L).ownerId(1L).name("item").description("description").available(true).build();

        Item item2 = Item.builder().id(1L).ownerId(1L).name("item").description("description").available(true).build();

        Item item3 = Item.builder().ownerId(1L).name("item").description("description").available(true).build();
        item3.setId(3L);

        Item item4 = Item.builder().id(1L).ownerId(1L)
                .name("another item")
                .description("description").available(true).build();

        Item item5 = Item.builder().id(1L).ownerId(1L).name("item").description("description").available(false).build();

        assertEquals(item1, item2);
        assertEquals(item1, item3);
        assertNotEquals(item1, item4);
        assertNotEquals(item1, item5);
    }
}