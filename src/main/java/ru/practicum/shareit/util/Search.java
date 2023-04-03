package ru.practicum.shareit.util;

import java.util.List;

/**
 * контракт функционала поиска объектов <p>
 * ТЗ-13
 */
public interface Search<T extends Identifiable> {

    List<T> findByQuery(String query);
}