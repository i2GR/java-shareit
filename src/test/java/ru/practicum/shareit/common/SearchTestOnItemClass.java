package ru.practicum.shareit.common;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.InMemoryItemStorage;
import ru.practicum.shareit.item.model.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchTestOnItemClass {

    InMemoryItemStorage storage = new InMemoryItemStorage();
    Item item1 = Item.builder().id(1L)
            .ownerId(1L)
            .name("item1")
            .description("some description")
            .available(true).build();

    Item item2 = Item.builder()
            .id(2L)
            .ownerId(1L)
            .name("ANOTHER ITEM")
            .description("another text")
            .available(true).build();

    Item item3 = Item.builder()
            .id(3L)
            .ownerId(1L)
            .name("item3")
            .description("another description")
            .available(false).build();

    Item item4 = Item.builder()
            .id(3L)
            .ownerId(1L)
            .name("item4")
            .description("another description")
            .available(true).build();

    @Test
    void findByQuery() {
        storage.create(item1);
        storage.create(item2);
        storage.create(item3);
        storage.create(item4);

        Item[] searchQueryOfItem = storage.findByQuery("item").toArray(Item[]::new);
        Item[] searchQueryOfITE = storage.findByQuery("ITE").toArray(Item[]::new);
        Item[] searchQueryOfAnother = storage.findByQuery("another").toArray(Item[]::new);
        Set<Item> searchAnotherSet = new HashSet<>(List.of(searchQueryOfAnother));

        assertEquals(0, storage.findByQuery(null).size());
        assertEquals(0, storage.findByQuery("").size());
        assertArrayEquals(searchQueryOfItem, searchQueryOfITE);
        assertEquals(3, searchQueryOfItem.length);
        assertEquals(2, searchQueryOfAnother.length);
        assertTrue(searchAnotherSet.contains(item2));
        assertTrue(searchAnotherSet.contains(item4));
    }
}