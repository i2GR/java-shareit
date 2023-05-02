package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.validation.OnCreate;
import ru.practicum.shareit.booking.validation.OnUpdate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
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

    @Email(message = "Bad User.email", groups = {OnCreate.class, OnUpdate.class}) //если передался, то д. соответствовать
    @NotEmpty(message = "Email cannot be empty", groups = {OnCreate.class}) //при патче может не передаваться ?
    private String email;

    @NotBlank(message = "name cannot be empty", groups = {OnCreate.class}) //при патче может не передаваться
    private String name;
}