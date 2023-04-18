package ru.practicum.shareit.util.inmemory;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.util.ShareItEntity;

/**
 * Отдельно выделеннный сервис с функционалом присвоением идентификатора для реализации ТЗ-13
 */
@Slf4j
@Component
@NoArgsConstructor
public class IdService {

    private long lastId = 0;

    public Long getNewId(ShareItEntity shareItEntity) {
        return shareItEntity.getId() != null ? shareItEntity.getId() : lastId + 1;
    }

    public void updateEntityWithId(ShareItEntity shareItEntity, Long id) {
        shareItEntity.setId(id);
        lastId = id;
    }
}