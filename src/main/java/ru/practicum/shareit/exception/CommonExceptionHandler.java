package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Обработчик исключений, определененных в ShareIt
 */
@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    /**
     * обработка исключения валидации параметров передаваемых в ендпойнты (HTTP-код 400)
     * @param exception экземпляр исключения Spring (MethodArgumentNotValidException) ошибки валидации параметров
     * @return сообщение об ошибке (ResponseEntity)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.info("Bad request");
        ErrorResponse error = new ErrorResponse("API validation error", "bad request body");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * обработка исключения ошибки эхранилища с отправкой (HTTP-код 406)
     * @param exception исключение валидации
    * @return сообщение об ошибке (ResponseEntity)
     */
    @ExceptionHandler(StorageErrorException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ErrorResponse handleStorageErrorException(StorageErrorException exception) {
        log.warn("Error operating storage: {}", exception.getMessage());
        return new ErrorResponse("Error operating storage", exception.getMessage());
    }

    /**
     * обработка исключения клонфликта данных в хранилище (HTTP-код 409)
     * @param exception исключение валидации
     * @return сообщение об ошибке (ResponseEntity)
     */
    @ExceptionHandler(StorageConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleConflictException(StorageConflictException exception) {
        log.warn("Conflict: {}", exception.getMessage());
        return new ErrorResponse("Conflict ", exception.getMessage());
    }

    /**
     * обработка исключения при получении непредусмотренных данных в сервис-слое (HTTP-код 400)
     * @param exception исключение
     * @return сообщение об ошибке (ResponseEntity)
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleServiceException(ServiceException exception) {
        log.warn(exception.getMessage());
        return new ErrorResponse("Service Error",  exception.getMessage());
    }

    /**
     * обработка исключения : запрошенный элемент не найден (HTTP-кода 404)
     * @param exception исключение
     * @return сообщение об ошибке (ResponseEntity)
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
     * @return сообщение об ошибке (ResponseEntity)
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse handleForbidden(ForbiddenException exception) {
        log.warn(exception.getMessage());
        return new ErrorResponse("Forbidden", exception.getMessage());
    }
}