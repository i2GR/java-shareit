package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

/**
 * интерфейс для Jpa-репозитория пользователей
 */
public interface UserRepository extends JpaRepository<User, Long> {
}