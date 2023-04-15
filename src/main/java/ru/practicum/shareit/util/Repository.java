package ru.practicum.shareit.util;

import java.util.List;
import java.util.Optional;

/**
 * Интерфкейс-шаблон для хранилища идентифицируемых объектов модели, имеющих идентификатор <p>
 * ТЗ-13 <p>
 * Предусмаривается простая модель "CRUD" <p>
 * @implNote <u>"идентифицируемый объект"</u> - это экземпляр DTO-класса, реализующий интерфейс {@link Entity} <p>
 * @param <T> указанный экземпляр Model-класса, имеющий идентификатора
 */
public interface Repository<T extends Entity> {

    /**
     * Сохранение экземпляра объекта
     * @param entity экземпляр для сохранения
     * @return Optional с экземпляром объекта
     */
    Optional<T> create(T entity);

    /**
     * Получение объекта из хранилища по идентификатору
     * @param id присвоенный идентификатор экземпляра объекта модели
     * @return Optional с экземпляром объекта
     */
    Optional<T> readById(Long id);

    /**
     * Модификация объекта в хранилище
     * @param entity новый экземпляр объекта модели
     * @return Optional с экземпляром объекта
     */
    Optional<T> update(T entity);

    /**
     * Получение списка всех фильмов или пользователей
     * @return список с хранимыми объектами
     */
    List<T> readAll();

    /**
     * Удаление экземпляра из хранилища <p>
     * @param id идентификатор экземпляра entity для удаления
     * @return Optional с хранимым объектом
     */
    Optional<T> delete(Long id);

}