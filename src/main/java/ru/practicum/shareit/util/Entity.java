package ru.practicum.shareit.util;

/**
 * Класс-маркер для реализации интерфейса получения сущностей приложения по идентификатору
 */
public abstract class Entity {

    private Long id;

    public abstract void setId(Long id);

    public abstract Long getId();
}