package ru.practicum.shareit.util;

/**
 * Интерфейс получения сущностей по идентификатору
 */
public interface Identifiable {

    void setId(Long id);

    Long getId();
}
