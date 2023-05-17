package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * интерфейс для Jpa-репозитория вещей
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * метод поиска <b>только доступных</b> вещей по имени или описанию
     * @param queryInName строка запроса по названию
     * @param queryInDescr эквивалентная строка запроса по описанию
     * @param pageable параметр постраничного вывода
     * @return список List
     */
    List<Item> findDistinctByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(String queryInName,
                                                                                   String queryInDescr,
                                                                                   Pageable pageable);

    /**
     * Поиск всех вещей по идентификатору владельца
     * @param ownerId идентификатор пользователя-владельца вещи
     * @param pageable параметр постраничного вывода
     * @return список List
     */
    List<Item> findByOwnerIdEquals(Long ownerId, Pageable pageable);

    List<Item> findAllByRequest_IdIn(List<Long> id);
}