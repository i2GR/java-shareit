package ru.practicum.shareit.user.model;

import lombok.*;
import ru.practicum.shareit.util.Identifiable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * Model-класс информации о пользователе <p>
 * ТЗ-13
 */
@Data
@Builder
@EqualsAndHashCode(exclude = {"id"})
public class User implements Identifiable {

    private Long id;
    @Email(message = "Bad User.email")
    private String email;
    @NotNull
    private String name;
}
