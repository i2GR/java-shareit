package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * интерфейс сервис-слой для обработки данных вещах для шаринга <p>
 * ТЗ-13 <p>
 * CRUD-функционал, метод поиска
 */
public interface ItemServing {
    Item addItem(Long ownerId, Item item);

    Item patch(Long ownerId, Long itemId, ItemDto dto);

    Item getById(Long itemId);

    List<Item> getAllByUserId(Long userId);

    Item deleteById(Long ownerId, Long itemId);

    List<Item> search(String query);
}