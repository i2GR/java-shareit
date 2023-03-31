package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * интерфейс сервис-слой для обработки данных вещах для шаринга <p>
 * ТЗ-13 <p>
 * CRUD-функционал
 */
public interface UserServing {
    User addUser(User user);

    User patch(Long userId, UserDto dto);

    User getById(Long userId);

    List<User> getAll();

    User deleteById(Long userId);
}
