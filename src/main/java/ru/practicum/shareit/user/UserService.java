package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

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
    public UserDto addUser(UserDto dto) {
        User user = userMapper.fromDto(dto);
        User created = userStorage.create(user).orElseThrow(
                                                    () -> {
                                                        log.info("Service error creating User");
                                                        throw new StorageErrorException("Service error creating User"); }
                                                    );
        return userMapper.toDto(created);
    }

    @Override
    public UserDto patch(Long userId, UserDto dto) {
        User user = userStorage.readById(userId).orElseThrow(
                                                     () -> {
                                                         log.info("Service error reading User#id {}", userId);
                                                         throw new StorageErrorException(
                                                         String.format("Service error reading User#id %d", userId)); }
                                                     );
        userMapper.update(dto, user);
        userStorage.update(user).orElseThrow(
                                                    () -> {
                                                        log.info("Service error patching User");
                                                        throw new StorageErrorException("Service error creating User"); }
        );
        return userMapper.toDto(user);
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userStorage.readById(userId).orElseThrow(
                                                    () -> {
                                                        log.info("User#id {} not found", userId);
                                                        throw new NotFoundException(
                                                        String.format("User#id %d not found", userId)); }
        );
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.readAll().stream()
                          .map(userMapper::toDto)
                          .collect(Collectors.toList());
    }

    @Override
    public String deleteById(Long userId) {
        userStorage.delete(userId).orElseThrow(
                                                    () -> {
                                                        log.info("Service error deleting User#id {}: null received",
                                                        userId);
                                                        throw new ServiceException(
                                                        String.format("received null deleting User#id %d", userId)); }
        );
        return SUCCESS_DELETE_MESSAGE;
    }
}