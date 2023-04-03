package ru.practicum.shareit.exception;

/**
 * исключение при операциях в слое хранилища при наличии конфликтов данных в хранилище сущностей <p>
 * выбрасывается в специально указанных случаях согласно "бизнес-логике"
 * (например) при обновлении данных (PATCH запросах)
 */
public class StorageConflictException extends RuntimeException {

    /**
     * @param message : передается информация об причине исключения
     */
    public StorageConflictException(String message) {
        super(message);
    }
}