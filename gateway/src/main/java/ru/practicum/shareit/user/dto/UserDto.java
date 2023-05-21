package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * DTO для класса User <p>
 * ТЗ-13
 */
@Getter
@Builder
@EqualsAndHashCode(exclude = {"id"})
public class UserDto {

    @Setter
    private Long id;

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE,
           message = "Bad User.email", groups = {OnCreate.class, OnUpdate.class}) //Если передался, то д. соответствовать
    @NotEmpty(message = "Email cannot be empty", groups = {OnCreate.class}) //при патче может не передаваться ?
    private String email;

    @NotBlank(message = "name cannot be empty", groups = {OnCreate.class}) //при патче может не передаваться
    private String name;
}