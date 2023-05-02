package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.validation.OnCreate;
import ru.practicum.shareit.booking.validation.OnUpdate;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * REST-Контроллер данных о пользователе (User)
 * <p> в качестве входных и выходных данных используется UserDto
 * <p> ТЗ-13
 * <p> CRUD-запросы + PATCH запрос
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserServiceImpl userService;

    @PostMapping
    public UserDto postUser(@RequestBody @Validated(value = OnCreate.class) UserDto dto) {
        log.info("[post] user http-request");
        return userService.addUser(dto);
    }

    @PatchMapping("/{userId}")
    public UserDto patchUser(@PathVariable(name = "userId") Long userId,
                             @Validated(value = OnUpdate.class) @RequestBody UserDto dto) {
        log.info("[patch] user http-request with id {}", userId);
        return userService.patch(userId, dto);
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable(name = "userId") Long userId) {
        log.info("[get] user http-request with id {}", userId);
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("[get] all users http-request");
        return userService.getAll();
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable(name = "userId") Long userId) {
        log.info("[delete] user http-request with id {}", userId);
        return userService.deleteById(userId);
    }
}