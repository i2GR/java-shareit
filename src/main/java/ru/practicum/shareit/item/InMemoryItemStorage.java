package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.util.inmemory.InMemoryStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryItemStorage extends InMemoryStorage<Item> implements InMemoryItemRepository {

    public InMemoryItemStorage() {
        super("Item", new HashSet<>());
    }

    /**
     * реализация создания нового объекта пользователя для отвязки от хранящегося in-memory в структуре данных
     * @implNote применен модификатор final, т.к. наследование не планируется
     * @param item копируемый экз. объекта для шаринга (из HashMap как структуры данных хранилища)
     * @return новый экз. объекта для шаринга, для корректного обновления (update) в Service-слое.
     */
    @Override
    protected final Item copy(Item item) {
        if (item == null) {
            log.warn("Item Not found reading repository");
            throw new NotFoundException("Item Not found reading repository");
        }
        return Item.builder()
                .id(item.getId())
                .ownerId(item.getOwnerId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    /**
     * метод поиска объектов для шаринга по вхождению строки запроса поиска <p>
     * поиск в названии (Item.name) и описании (Item.description)
     * @param query строка поиска
     * @return список объектов, для которых в названии (name) и описании (description) нашлось вхождение строки поиска
     */
    public List<Item> findByQuery(String query) {
        if (query == null || query.isBlank()) {
            log.info("search query is null or blank");
            return List.of();
        }
        return idMapEntity.values().stream()
                .filter(v -> v.getAvailable().equals(Boolean.TRUE))
                .filter(v ->
                        v.getName().toLowerCase().contains(query.toLowerCase())
                                || v.getDescription().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}