package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import static java.util.stream.Collectors.toList;
import static java.lang.String.format;

import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

/**
 * сервис-слой для обработки данных пользователей <p>
 * ТЗ-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDtoMapper userMapper;

    private final UserRepository userStorage;

    @Transactional
    @Override
    public UserDto addUser(UserDto dto) {
        User user = userMapper.fromDto(dto);
        User created = userStorage.save(user);
        return userMapper.toDto(created);
    }

    @Transactional
    @Override
    public UserDto patch(Long userId, UserDto dto) {
        User user = userStorage.findById(userId).orElseThrow(
                () -> {
                    log.info("User#id {} not found", userId);
                    return new NotFoundException(format("User#id %d not found", userId));
                }
        );
        userMapper.update(dto, user);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userStorage.findById(userId).orElseThrow(
                () -> {
                    log.info("User#id {} not found", userId);
                    return new NotFoundException(format("User#id %d not found", userId));
                }
        );
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                          .map(userMapper::toDto)
                          .collect(toList());
    }

    @Transactional
    @Override
    public String deleteById(Long userId) {
        if (userStorage.existsById(userId)) {
            userStorage.deleteById(userId);
            log.info("deleted item with id {}", userId);
            return SUCCESS_DELETE_MESSAGE;
        }
        throw new BadRequestException(format("Error deleting User#id %d", userId));
    }
}