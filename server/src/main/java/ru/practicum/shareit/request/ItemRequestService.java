package ru.practicum.shareit.request;


import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestReplyDto;

import java.util.List;

/**
 * интерфейс сервис-слоя для обработки запросов вещей <p>
 * ТЗ-15 <p>
 */
public interface ItemRequestService {

    /**
     * добавление запроса вещи
     * @param userId идентификатор пользователя, подающего запрос
     * @param dto DTO запроса вещи
     * @return Экз. ReplyDTO для запроса вещи
     */
    ItemRequestReplyDto addRequest(Long userId, ItemRequestDto dto);

    /**
     * Получение списка запросов вещей для пользователя <p>
     * Выполняется для автора запросов, по идентификатору пользователя
     * @param userId идентификатор пользователя-автора запросов, для которого формируется список
     * @return список ReplyDTO запроса вещей (включая список ответов на запросы)
     */
    List<ItemRequestReplyDto> getRequestsByUserId(Long userId);

    /**
     * Получение списка запросов, созданных другими пользователями <p>
     * (просмотр существующих запросы, на которые могли бы ответить пользователи)
     * @param from индекс первого элемента (нумерация начинается с 0)
     * @param size количество элементов для отображения
     * @param userId идентификатор пользователя-участника "шариэта"
     * @return список ReplyDTO запроса вещей (включая список ответов на запросы)
     */
    List<ItemRequestReplyDto> getAllRequestsByAnotherUsers(Long from, Integer size, Long userId);

    /**
     * Получение данных об одном конкретном запросе <p>
     * (вместе с данными об ответах на него @link{ItemRequestService#getRequestsByUserId})<p>
     * Посмотреть данные об отдельном запросе может любой пользователь<p>
     * @param requestId идентификатор запроса
     * @param userId идентификатор пользователя-участника "шариэта"
     * @return ReplyDTO запроса вещей (включая список ответов на запросы)
     */
    ItemRequestReplyDto getRequestById(Long requestId, Long userId);
}
