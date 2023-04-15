package ru.practicum.shareit.exception;

/**
 * исключение при операциях в слое хранилища при ошибкке операций с хранилищем <p>
 * выбрасывается в общем случае и в случае исключений классов/методов JAVA
 */
public class StorageErrorException extends RuntimeException {

    /**
     * @param message : передается информация об причине исключения
     *                (при наличии, или сообщение исключений классов/методов JAVA)
     */
    public StorageErrorException(String message) {
        super(message);
    }
}