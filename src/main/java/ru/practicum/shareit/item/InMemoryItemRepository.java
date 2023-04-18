package ru.practicum.shareit.item;

import ru.practicum.shareit.util.Repository;
import ru.practicum.shareit.util.Search;
import ru.practicum.shareit.item.model.Item;

/**
 * интерфейс-маркер для in-memory репозитория вещей <p>
 * ТЗ-13 <p>
 * CRUD-операции, метод поиска
 */
public interface InMemoryItemRepository extends Repository<Item>, Search<Item> {
}