package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

/**
 * интерфейс для Jpa-репозитория запросов вещей (Request)
 */
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    /**
     * получение списка запросов на вещи от пользователя <p>
     * сортировка <p>
     * сортировка от более новых к более старым (возрастание времени создания запроса)
     * @param userId идентификатор пользователя-заказчика
     * @return список (List)
     */
    List<ItemRequest> findByRequesterIdOrderByCreatedAsc(Long userId);

    /**
     * Получение списка запросов, созданных другими пользователями <p>
     * (просмотр существующих запросы, на которые могли бы ответить пользователи)
     * @param ownerId идентификатор пользователя - его запросы исключаются из результатов выдачи
     * @param pageable параметр постраничного вывода

     * @return список ReplyDTO запроса вещей (включая список ответов на запросы)
     */
     List<ItemRequest> findAllByRequesterIdNot(Long ownerId, Pageable pageable);
}