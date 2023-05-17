package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.validation.OnCreate;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

/**
 * REST-Контроллер запросов вещей от пользователей (ItemRequest)
 * методы шлюза. Описание и назначение - по методам Сервера (модуль shareit-server) <p>
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addNewItemRequest(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId,
                                                 @RequestBody @Validated(value = OnCreate.class) ItemRequestDto dto) {
        log.info("[post] item-request http-request");
        return requestClient.addRequest(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequestsByUserId(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId) {
        log.info("[get] item-request of user with id {} http-request", userId);
        return requestClient.getRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequestsByAnotherUsers(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId,
                                                                  @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Long from,
                                                                  @RequestParam(name = "size", defaultValue = "20") @Positive Integer size) {
        log.info("[get] all item-requests");
        return requestClient.getAllRequestsByAnotherUsers(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long userId,
                                              @PathVariable Long requestId) {
        log.info("[get] item-request by id {}", requestId);
        return requestClient.getRequestById(requestId, userId);
    }
}