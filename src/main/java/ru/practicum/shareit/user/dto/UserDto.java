package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.validation.OnCreate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * DTO для класса User <p>
 * ТЗ-13
 */
@Getter
@Builder
@EqualsAndHashCode(exclude = {"id"}, callSuper = false)
public class UserDto {

    @Setter
    private Long id;

    @Email(message = "Bad User.email", groups = {OnCreate.class})
    @NotEmpty(message = "Email cannot be empty", groups = {OnCreate.class})
    private String email;

    @NotEmpty(message = "name cannot be empty", groups = {OnCreate.class})
    private String name;
}
