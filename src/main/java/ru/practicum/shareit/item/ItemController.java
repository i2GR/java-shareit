package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.validation.OnCreate;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

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

    private final ItemService itemService;

    /**
     * Создание вещи для шаринга
     * @param ownerId идентификатор пользователя - владельца вещи
     * @param dto DTO-класс сущности вещи для шаринга
     * @return DTO-класс сущности вещи для шаринга, со всеми сохраненными данными в приложении
     */
    @PostMapping
    public ItemDto postItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                            @RequestBody @Validated(value = OnCreate.class) ItemDto dto) {
        log.info("[post] item http-request with owner id {}", ownerId);
        return itemService.addItem(ownerId, dto);
    }

    /**
     * Внесение изменений в сущность вещи для шаринга
     * @param ownerId идентификатор пользователя - владельца вещи
     * @param itemId идентификатор вещи для шаринга
     * @return DTO-класс сущности вещи для шаринга, с сохраненными данными в приложении
     */
    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                             @PathVariable(name = "itemId") Long itemId,
                             @RequestBody ItemDto dto) {
        log.info("[patch] item http-request with id {} with owner id {}", itemId, ownerId);
        return itemService.patch(ownerId, itemId, dto);
    }

    /**
     * Получение информации о вещи для шаринга
     * @param itemId идентификатор вещи для шаринга
     * @return DTO-класс сущности вещи для шаринга, с сохраненными данными в приложении
     */
    @GetMapping("/{itemId}")
    public ItemResponseDto getItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                                   @PathVariable(name = "itemId") Long itemId) {
            log.info("[get] item http-request with id {} by user with id {}", itemId, ownerId);
            return itemService.getByOwnerById(ownerId, itemId);
    }

    /**
     * Получение информации о вещи для шаринга
     * @param ownerId идентификатор владельца
     * @return Список с DTO-классами сущностей вещей для шаринга, с сохраненными данными в приложении
     */
    @GetMapping
    public List<ItemResponseDto> getAllByUserId(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId) {
        log.info("[get] all items http-request with userId {}", ownerId);
        return itemService.getAllByUserId(ownerId);
    }

    /**
     * Удаление  вещи для шаринга
     * @param ownerId идентификатор владельца
     * @param itemId идентификатор вещи для шаринга
     * @return сообщение об удалении
     */
    @DeleteMapping("/{itemId}")
    public String deleteItem(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long ownerId,
                             @PathVariable(name = "itemId") Long itemId) {
        log.info("[delete] item http-request with id {} from user id {}", itemId, ownerId);
        return itemService.deleteById(ownerId, itemId);
    }

    /**
     * Поиск вещи для шаринга по названию и/или описанию вещи
     * @param query текстовый запрос для поиска
     * @return Список с DTO-классами сущностей вещей для шаринга, удовлетворяющих критериям поиска
     */
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(name = "text") String query) {
        log.info("Search [get] items http-request of query {}", query);
        return itemService.search(query);
    }

    /**
     * Добавление комментария к вещи
     * @param authorId идентификатор владельца
     * @param itemId идентификатор вещи для шаринга
     * @param dto DTO-класс сущности вещи для шаринга
     * @return DTO-класс сущности вещи для шаринга, с сохраненными данными в приложении
     */
    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(value = SHARER_USER_HTTP_HEADER) Long authorId,
                                         @PathVariable(name = "itemId") Long itemId,
                                         @RequestBody @Validated(value = OnCreate.class) CommentDto dto) {
        log.info("[post] comment http-request to item with id {} from user@id {}", itemId, authorId);
        return itemService.addComment(authorId, itemId, dto);
    }
}