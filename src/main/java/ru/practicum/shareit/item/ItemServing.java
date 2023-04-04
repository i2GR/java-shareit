package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * интерфейс сервис-слой для обработки данных вещах для шаринга <p>
 * ТЗ-13 <p>
 * CRUD-функционал, метод поиска
 */
public interface ItemServing {

    /**
     * добавление вещи
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @return экз. DTO для добавленного  пользователя
     */
    ItemDto addItem(Long ownerId, ItemDto itemDto);

    /**
     * частичное изменение данных о вещи
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param itemId идентификатор сохраненной вещи
     * @param dto DTO для вещи <p>
     * частично заполненные поля
     * @return экз. DTO для добавленного  пользователя
     */
    ItemDto patch(Long ownerId, Long itemId, ItemDto dto);

    /**
     * получение DTO для вещи из хранилища
     * @param itemId идентификатор сохраненной вещи
     * @return экз. DTO для вещи из хранилища
     */
    ItemDto getById(Long itemId);

    /**
     * получение списка DTO для всех вещей из хранилища
     * @return список DTO
     */
    List<ItemDto> getAllByUserId(Long userId);

    /**
     * удаление пользователя из хранилища <p>
     * @implNote если вещь не принадлежит пользователю, она не должна быть удалена
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param itemId идентификатор сохраненной вещи
     * @return DTO для пользователя (уудаленный пользователь)
     */
    String deleteById(Long ownerId, Long itemId);

    /**
     * поиск вещей по текстовому запросу
     * @param query строковое представление запроса - клчювое сочетание для поиска
     * @return список DTO, для которых было найдено совпадение
     */
    List<ItemDto> search(String query);
}