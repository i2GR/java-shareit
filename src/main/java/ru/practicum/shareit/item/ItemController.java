package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        log.info("[post] item http-request with owner id {}" , ownerId);
        Item item = ItemDtoMapper.INSTANCE.fromDto(dto);
        return ItemDtoMapper.INSTANCE.toDto(itemService.addItem(ownerId, item));
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId, @PathVariable(name = "itemId") Long itemId, @RequestBody ItemDto dto) {
        log.info("[patch] item http-request with id {} with owner id {}", itemId, ownerId);
        return ItemDtoMapper.INSTANCE.toDto(itemService.patch(ownerId, itemId, dto));
    }
    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable(name = "itemId") Long itemId) {
        log.info("[get] item http-request with id {}", itemId);
        return ItemDtoMapper.INSTANCE.toDto(itemService.getById(itemId));
    }

    @GetMapping
    public List<ItemDto> getAllByUserId(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId) {
        log.info("[get] all items http-request with userId {}", ownerId);
        return itemService.getAllByUserId(ownerId).stream()
                .map(ItemDtoMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public String deleteItem(@RequestHeader(value = "X-Sharer-User-Id") Long ownerId, @PathVariable(name = "itemId") Long itemId) {
        log.info("[delete] item http-request with id {} from user id {}", itemId, ownerId);
        return ItemDtoMapper.INSTANCE.toDto(itemService.deleteById(ownerId, itemId)).getId().equals(itemId)
               ? "deleted"
               : "API Delete Error";
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems (@RequestParam(name = "text") String query) {
        log.info("Search [get] items http-request of query {}", query);
        return !query.isBlank() ?
               itemService.search(query).stream()
               .map(ItemDtoMapper.INSTANCE::toDto)
               .collect(Collectors.toList())
               : new ArrayList<>();
    }
}