package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.util.Entity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * DTO для класса User <p>
 * ТЗ-13
 */
@Getter
@Builder
public class UserDto extends Entity {

    @Setter
    private Long id;

    @Email(message = "Bad User.email")
    @NotEmpty(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be null")
    private String email;

    @NotEmpty(message = "name cannot be empty")
    @NotNull
    private String name;
}
