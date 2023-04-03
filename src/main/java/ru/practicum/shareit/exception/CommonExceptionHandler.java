package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Обработчик исключений, определененных в ShareIt
 */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * метод обработки исключения валидации параметров передаваемых в ендпойнты
     * @param exception экземпляр исключения Spring (MethodArgumentNotValidException) ошибки валидации параметров
     * @return List со списком сообщений об ошибке (ResponseEntity), c указание имени поля и значением из тела запроса,
     * которые вызвали ошибку
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<List<ErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.info("Bad request");
        List<ErrorResponse> errors = new ArrayList<>();
        errors.add(new ErrorResponse("error", "Invalid request body"));
        errors.addAll(
              exception.getBindingResult().getFieldErrors()
              .stream()
              .map(e -> new ErrorResponse("error in field: " + e.getField(),
                                      "bad value:" + e.getRejectedValue()))
              .collect(Collectors.toList()));
        errors.forEach(e -> log.info(e.getError() + " " + e.getDescription()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StorageErrorException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ErrorResponse handleStorageErrorException(StorageErrorException exception) {
        log.warn("Error operating storage: {}", exception.getMessage());
        return new ErrorResponse("Error operating storage", exception.getMessage());
    }

    @ExceptionHandler(StorageConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleConflictException(StorageConflictException exception) {
        log.warn("Conflict: {}", exception.getMessage());
        return new ErrorResponse("Conflict ", exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNoElement(NoSuchElementException exception) {
        log.warn("Not Found {}", exception.getMessage());
        return new ErrorResponse("Error operating storage / Not found", exception.getMessage());
    }

    /**
     * метод обработки исключения при получении непредусмотренных данных в сервис-слое
     * @param exception исключение
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceException(ServiceException exception) {
        log.warn(exception.getMessage());
        return new ErrorResponse("Service Error",  exception.getMessage());
    }

    /**
     * обработка исключения с отправкой HTTP-кода 404
     * @param exception исключение
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNotFound(NotFoundException exception) {
        log.warn(exception.getMessage());
        return new ErrorResponse("Not found", exception.getMessage());
    }

    /**
     * обработка исключения с отправкой HTTP-кода 403
     * @param exception исключение
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleForbidden(ForbiddenException exception) {
        log.warn(exception.getMessage());
        return new ErrorResponse("Forbidden", exception.getMessage());
    }
}