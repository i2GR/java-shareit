package ru.practicum.shareit.exception;

/**
 * исключение при операциях в слое хранилища при отсутствии в хранилище сущности (целиком или по идентификатору)
 * выбрасывается в специально указанных случаях согласно "бизнес-логике"
 */
public class NotFoundException extends RuntimeException{

    /**
     * @param message : передается информация об причине исключения
     *                (при наличии, или сообщение исключений классов/методов JAVA)
     */
    public NotFoundException(String message) {
        super(message);
    }
}