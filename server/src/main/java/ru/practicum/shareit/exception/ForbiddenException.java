package ru.practicum.shareit.exception;

/**
 * исключение при операциях в слое хранилища при нарушении прав доступа
 * выбрасывается в специально указанных случаях согласно "бизнес-логике"
 */
public class ForbiddenException extends RuntimeException {

    /**
     * @param message : передается информация об причине исключения
     */
    public ForbiddenException(String message) {
        super(message);
    }
}