package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * класс ответа на HTTP-запрос при выбрасывании исключения на Ендпойнте
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String error;
    private final List<String> trace;
}