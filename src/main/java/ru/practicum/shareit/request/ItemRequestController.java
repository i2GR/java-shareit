package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.validation.OnCreate;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestReplyDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

/**
 * REST-Контроллер запросов вещей от пользователей (ItemRequest)
 * <p> в качестве входных и выходных данных используется Dto
 * <p> ТЗ-15
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    /**
     * добавление запроса на вещь
     * @param userId идентификатор пользователя, размещающего запрос
     * @param dto ДТО-объект запроса
     * @return ReplyDto-объект запроса
     */
    @PostMapping
    public ItemRequestReplyDto addNewItemRequest(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId,
                                              @RequestBody @Validated(value = OnCreate.class) ItemRequestDto dto) {
            log.info("[post] item-request http-request");
            return itemRequestService.addRequest(userId, dto);
    }

    /**
     * Получение списка запросов вещей для пользователя <p>
     * Выполняется для автора запросов, по идентификатору пользователя
     * @param userId идентификатор пользователя-автора запросов, для которого формируется список
     * @return список ReplyDTO запроса вещей (включая список ответов на запросы)
     */
    @GetMapping
    public List<ItemRequestReplyDto> getRequestsByUserId(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId) {
        log.info("[get] item-request of user with id {} http-request", userId);
        return itemRequestService.getRequestsByUserId(userId);
    }

    /**
     * Получение списка запросов, созданных другими пользователями <p>
     * (просмотр существующих запросы, на которые могли бы ответить пользователи)
     * @param from индекс первого элемента (нумерация начинается с 0)
     * @param size количество элементов для отображения
     * @param userId идентификатор пользователя-участника "шариэта"
     * @return список ReplyDTO запроса вещей (включая список ответов на запросы)
     */
    @GetMapping("/all")
    public List<ItemRequestReplyDto> getAllRequestsByAnotherUsers(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId,
                                             @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Long from,
                                             @RequestParam(name = "size", defaultValue = "20") @Positive Integer size) {
        log.info("[get] all item-requests");
        return itemRequestService.getAllRequestsByAnotherUsers(from, size, userId);
    }

    /**
     * Получение данных об одном конкретном запросе <p>
     * (вместе с данными об ответах на него @link{ItemRequestService#getRequestsByUserId})<p>
     * Посмотреть данные об отдельном запросе может любой пользователь<p>
     * @param requestId идентификатор запроса
     * @param userId идентификатор пользователя "шариэта"
     * @return ReplyDTO запроса вещей (включая список ответов на запросы)
     */
    @GetMapping("/{requestId}")
    public ItemRequestReplyDto getRequestById(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId,
                                             @PathVariable Long requestId) {
        log.info("[get] item-request by id {}", requestId);
        return itemRequestService.getRequestById(requestId, userId);
    }
}