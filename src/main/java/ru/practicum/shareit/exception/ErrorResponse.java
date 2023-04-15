package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * класс ответа на HTTP-запрос при выбрасывании исключения на Ендпойнте
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final String error;

    private final String description;
}