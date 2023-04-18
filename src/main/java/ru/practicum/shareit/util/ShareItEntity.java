package ru.practicum.shareit.util;

/**
 * Класс-маркер для реализации интерфейса получения сущностей приложения по идентификатору
 */
public abstract class ShareItEntity {

    private Long id;

    public abstract void setId(Long id);

    public abstract Long getId();
}