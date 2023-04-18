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
        User created = userStorage.save(user);
        return userMapper.toDto(created);
    }

    @Override
    public UserDto patch(Long userId, UserDto dto) {
        User user = userStorage.findById(userId).orElseThrow(
                () -> {
                    log.info("Service error reading User#id {}", userId);
                    throw new StorageErrorException(String.format("Service error reading User#id %d", userId));
                }
        );
        userMapper.update(dto, user);
        userStorage.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userStorage.findById(userId).orElseThrow(
                () -> {
                    log.info("User#id {} not found", userId);
                    throw new NotFoundException(String.format("User#id %d not found", userId));
                }
        );
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        //TODO limits
        return userStorage.findAll().stream()
                          .map(userMapper::toDto)
                          .collect(Collectors.toList());
    }

    @Override
    public String deleteById(Long userId) {
        if (userStorage.existsById(userId)) {
            userStorage.deleteById(userId);
            log.info("deleted item with id {}", userId);
            return SUCCESS_DELETE_MESSAGE;
        }
        throw new BadRequestException(String.format("Error deleting User#id %d", userId));
    }
}