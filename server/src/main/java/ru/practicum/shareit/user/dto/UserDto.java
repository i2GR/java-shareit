package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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

    private String email;

    private String name;
}