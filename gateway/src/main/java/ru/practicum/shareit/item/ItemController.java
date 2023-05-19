package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

/**
 * REST-Контроллер данных о пользователе (User) <p>
 * методы шлюза. Описание и назначение - по методам Сервера (модуль shareit-server) <p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/items")
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> postItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                                           @RequestBody @Validated(value = OnCreate.class) ItemDto dto) {
        log.info("[post] item http-request with owner id {}", ownerId);
        return itemClient.addItem(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                             @PathVariable(name = "itemId") Long itemId,
                             @RequestBody ItemDto dto) {
        log.info("[patch] item http-request with id {} with owner id {}", itemId, ownerId);
        return itemClient.updateItem(ownerId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                                   @PathVariable(name = "itemId") Long itemId) {
            log.info("[get] item http-request with id {} by user with id {}", itemId, ownerId);
            return itemClient.getByOwnerById(ownerId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUserId(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                                                @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Long from,
                                                @RequestParam(name = "size", defaultValue = "20") @Positive Integer size) {
        log.info("[get] all items http-request with userId {}", ownerId);
        return itemClient.getAllByUserId(from, size, ownerId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                             @PathVariable(name = "itemId") Long itemId) {
        log.info("[delete] item http-request with id {} from user id {}", itemId, ownerId);
        return itemClient.deleteById(ownerId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(name = "text") String query,
                                     @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Long from,
                                     @RequestParam(name = "size", defaultValue = "20") @Positive Integer size) {
        log.info("Search [get] items http-request of query {}", query);
        return itemClient.searchItems(query, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long authorId,
                                         @PathVariable(name = "itemId") Long itemId,
                                         @RequestBody @Validated(value = OnCreate.class) CommentDto dto) {
        log.info("[post] comment http-request to item with id {} from user@id {}", itemId, authorId);
        return itemClient.addComment(authorId, itemId, dto);
    }
}