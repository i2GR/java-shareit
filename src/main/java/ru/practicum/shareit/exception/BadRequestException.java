package ru.practicum.shareit.exception;

/**
 * исключение при ошибке на уровне слоя
 * выбрасывается в общем случае и в случае исключений классов/методов JAVA
 */
public class BadRequestException extends RuntimeException {

    /**
     * исключение класса UserService при ошибке
     * @param message : передается информация об причине исключения
     *                (при наличии, или сообщение исключений классов/методов JAVA)
     */
    public BadRequestException(String message) {
        super(message);
    }
}