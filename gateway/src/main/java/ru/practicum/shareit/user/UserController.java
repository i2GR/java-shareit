package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.Positive;

/**
 * REST-Контроллер данных о пользователе (User)
 * методы шлюза. Описание и назначение - по методам Сервера (модуль shareit-server) <p>
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> postUser(@RequestBody @Validated(value = OnCreate.class) UserDto dto) {
        log.info("Creating new user");
        return userClient.addUser(dto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> patchUser(@Positive @PathVariable(name = "userId") long userId,
                             @Validated(value = OnUpdate.class) @RequestBody UserDto dto) {
        log.info("Patching user with id {}", userId);
        return userClient.patchUser(userId, dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@Positive @PathVariable(name = "userId") long userId) {
        log.info("Getting user by id = {}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Getting all users");
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@Positive @PathVariable(name = "userId") long userId) {
        log.info("Deleting user by id = {}", userId);
        return userClient.deleteUserById(userId);
    }
}