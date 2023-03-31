package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * класс ответа на HTTP-запрос при выбрасывании исключения на Ендпойнте
 */
@AllArgsConstructor
public class ErrorResponse {

    @Getter
    private final String error;
    @Getter
    private final String description;
}