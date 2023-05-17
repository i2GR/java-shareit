package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * Интерфейс сервис-слоя для обработки данных о вещах для шаринга <p>
 * ТЗ-13 <p>
 * CRUD-функционал
 */
public interface UserService {

    /**
     * добавление пользователя
     * @param dto Экз. DTO класса для пользователя
     * @return Экз. DTO для добавленного пользователя
     */
    UserDto addUser(UserDto dto);

    /**
     * частичное изменение данных пользователя
     * @param userId идентификатор сохраненного пользователя
     * @param dto DTO для пользователя <p>
     * частично заполненные поля
     * @return Экз. DTO для добавленного пользователя
     */
    UserDto patch(Long userId, UserDto dto);

    /**
     * получение DTO для пользователя из хранилища
     * @param userId идентификатор сохраненного пользователя
     * @return Экз. DTO для пользователя из хранилища
     */
    UserDto getById(Long userId);

    /**
     * получение списка DTO для всех пользователей из хранилища
     * @return список DTO
     */
    List<UserDto> getAll();

    /**
     * удаление пользователя из хранилища
     * @param userId идентификатор сохраненного пользователя
     * @return DTO для пользователя (удаленный пользователь)
     */
    String deleteById(Long userId);
}