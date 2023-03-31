package ru.practicum.shareit.user;

import ru.practicum.shareit.util.Repository;
import ru.practicum.shareit.user.model.User;

/**
 * интерфейс-маркер для in-memory репозитория пользователей <p>
 * ТЗ-13 <p>
 * CRUD-операции
 */
public interface UserRepository extends Repository<User> {
}
