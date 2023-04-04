package ru.practicum.shareit.user.model;

import lombok.*;
import ru.practicum.shareit.util.Entity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * Model-класс информации о пользователе <p>
 * ТЗ-13
 */
@Data
@Builder
@EqualsAndHashCode(exclude = {"id"}, callSuper = false)
public class User extends Entity {

    private Long id;

    @Email(message = "Bad User.email")
    private String email;

    @NotNull
    private String name;
}