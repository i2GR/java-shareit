package ru.practicum.shareit.util.inmemory;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.util.Identifiable;

/**
 * Отдельно выделеннный сервис с функционалом присвоением идентификатора для реализации ТЗ-13
 */
@Slf4j
@Component
@NoArgsConstructor
public class IdService {

    private long lastId = 0;

    public Long getNewId(Identifiable entity) {
        return entity.getId() != null ? entity.getId() : lastId + 1;
    }

    public Identifiable updateEntityWithId(Identifiable entity, Long id) {
        entity.setId(id);
        lastId = id;
        return entity;
    }
}