package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

/**
 * интерфейс сервис-слой для обработки данных о вещах для шаринга <p>
 * ТЗ-13 <p>
 * CRUD-функционал, метод поиска
 */
public interface ItemService {

    /**
     * добавление вещи
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @return Экз. DTO для добавленного пользователя
     */
    ItemDto addItem(Long ownerId, ItemDto itemDto);

    /**
     * частичное изменение данных о вещи
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param itemId идентификатор сохраненной вещи
     * @param dto DTO для вещи <p>
     * частично заполненные поля
     * @return Экз. DTO для добавленного пользователя
     */
    ItemDto patch(Long ownerId, Long itemId, ItemDto dto);

    /**
     * получение DTO для вещи из хранилища
     * @param userId идентификатор пользователя, сделавшего Http-запрос
     * @param itemId идентификатор сохраненной вещи
     * @return экз. DTO для вещи из хранилища
     */
    ItemResponseDto getByOwnerById(Long userId, Long itemId);

    /**
     * получение списка DTO для всех вещей из хранилища
     * @return список DTO
     */
    List<ItemResponseDto> getAllByUserId(Long userId);

    /**
     * удаление пользователя из хранилища <p>
     * @implNote если вещь не принадлежит пользователю, она не должна быть удалена
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param itemId идентификатор сохраненной вещи
     * @return DTO для пользователя (удаленный пользователь)
     */
    String deleteById(Long ownerId, Long itemId);

    /**
     * поиск вещей по текстовому запросу
     * @param query строковое представление запроса
     * @return список DTO, для которых было найдено совпадение
     */
    List<ItemDto> search(String query);

    /**
     * Добавление комментария к вещи
     * @param authorId идентификатор владельца
     * @param itemId идентификатор вещи для шаринга
     * @param dto DTO-класс сущности вещи для шаринга
     * @return DTO-класс сущности вещи для шаринга, с сохраненными данными в приложении
     */
    CommentResponseDto addComment(Long authorId, Long itemId, CommentDto dto);
}