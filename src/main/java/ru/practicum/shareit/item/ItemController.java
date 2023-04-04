package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

import javax.validation.Valid;
import java.util.List;

/**
 * REST-Контроллер данных о пользователе (User) <p>
 * в качестве входных и выходных данных используется UserDto <p>
 * ТЗ-13 <p>
 * CRUD-запросы + PATCH запрос <p>
 * запрос поиска
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/items")
public class ItemController {

    @NonNull
    private final ItemServing itemService;

    @PostMapping
    public ItemDto postItem(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId, @RequestBody @Valid ItemDto dto) {
        log.info("[post] item http-request with owner id {}", ownerId);
        return itemService.addItem(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                             @PathVariable(name = "itemId") Long itemId,
                             @RequestBody ItemDto dto) {
        log.info("[patch] item http-request with id {} with owner id {}", itemId, ownerId);
        return itemService.patch(ownerId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable(name = "itemId") Long itemId) {
        log.info("[get] item http-request with id {}", itemId);
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByUserId(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId) {
        log.info("[get] all items http-request with userId {}", ownerId);
        return itemService.getAllByUserId(ownerId);
    }

    @DeleteMapping("/{itemId}")
    public String deleteItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                             @PathVariable(name = "itemId") Long itemId) {
        log.info("[delete] item http-request with id {} from user id {}", itemId, ownerId);
        return itemService.deleteById(ownerId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(name = "text") String query) {
        log.info("Search [get] items http-request of query {}", query);
        return itemService.search(query);
    }
}