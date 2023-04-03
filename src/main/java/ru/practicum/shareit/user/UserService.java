package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

/**
 * сервис-слой для обработки данных пользователей <p>
 * ТЗ-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserServing {

    @NonNull
    private final UserDtoMapper userMapper;

    @NonNull
    private final UserRepository userStorage;

    @Override
    public User addUser(User user) {
        return userStorage.create(user).orElseThrow(
                                                    () -> {
                                                        log.info("Service error creating User");
                                                        throw new StorageErrorException("Service error creating User"); }
                                                    );
    }

    @Override
    public User patch(Long userId, UserDto dto) {
        User user = userStorage.readById(userId).orElseThrow(
                                                     () -> {
                                                         log.info("Service error reading User#id {}", userId);
                                                         throw new StorageErrorException(
                                                         String.format("Service error reading User#id %d", userId)); }
                                                     );
        userMapper.update(dto, user);
        return userStorage.update(user).orElseThrow(
                                                    () -> {
                                                        log.info("Service error patching User");
                                                        throw new StorageErrorException("Service error creating User"); }
        );
    }

    @Override
    public User getById(Long userId) {
        return userStorage.readById(userId).orElseThrow(
                                                    () -> {
                                                        log.info("User#id {} not found", userId);
                                                        throw new NotFoundException(
                                                        String.format("User#id %d not found", userId)); }
        );
    }

    @Override
    public List<User> getAll() {
        return userStorage.readAll();
    }

    @Override
    public User deleteById(Long userId) {
        return userStorage.delete(userId).orElseThrow(
                                                    () -> {
                                                        log.info("Service error deleting User#id {}: null received",
                                                        userId);
                                                        throw new ServiceException(
                                                        String.format("received null deleting User#id %d", userId)); }
        );
    }

}