package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ServiceException;
import ru.practicum.shareit.exception.StorageErrorException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * сервис-слой для обработки данных вещах для шаринга <p>
 * ТЗ-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService implements ItemServing {

    @NonNull
    private final ItemDtoMapper itemMapper;

    @NonNull
    private final UserService userService;

    @NonNull
    private final ItemRepository itemStorage;

    @Override
    public Item addItem(Long ownerId, Item item) {
        assignItemWithOwner(ownerId, item);
        return itemStorage.create(item).orElseThrow(
                                                    () -> {log.info("Service error creating Item");
                                                           throw new StorageErrorException("Service error creating Item");}
                                                    );
    }

    @Override
    public Item patch(Long ownerId, Long itemId, ItemDto dto) {
        Item item = itemStorage.readById(itemId).orElseThrow(
                                                     () -> {log.info("Service error reading Item#id {}", itemId);
                                                            throw new StorageErrorException(
                                                            String.format("Service error reading Item#id %d", itemId));}
                                                     );
        checkModificationAccess(ownerId, item.getOwnerId());
        itemMapper.update(dto, item);
        return itemStorage.update(item).orElseThrow(
                                                    () -> {log.info("Service error patching Item");
                                                           throw new StorageErrorException("Service error creating Item");}
        );
    }

    @Override
    public Item getById(Long itemId) {
        return itemStorage.readById(itemId).orElseThrow(
                                                    () -> {log.info("Service error reading Item#id {}", itemId);
                                                           throw new StorageErrorException(
                                                           String.format("Service error reading Item#id %d", itemId));}
        );
    }

    @Override
    public List<Item> getAllByUserId(Long userId) {
        //реализация для in-memory репозитория
        return itemStorage.readAll().stream()
                                    .filter(i -> i.getOwnerId().equals(userId))
                                    .collect(Collectors.toList());
    }

    @Override
    public Item deleteById(Long ownerId, Long itemId) {
        //реализация для in-memory репозитория. при работе с БД проверку лучше (и кажется, можно) делать запросом
        checkModificationAccess(ownerId, getById(itemId).getOwnerId());
        return itemStorage.delete(itemId).orElseThrow(
                                                    () -> {log.info("Service error deleting Item#id {}: null received", itemId);
                                                        throw new ServiceException(
                                                        String.format("received null deleting Item#id %d", itemId));}
        );
    }

    @Override
    public List<Item> search(String query) {
        return itemStorage.findByQuery(query);
    }

    /**
     * ТЗ-13
     * <p> вспомогательный метод проверки принадлежности вещи пользователю
     * <p> реализация для in-memory репозитория
     * <p> при реалзиации репозитория в БД проверка может быть осуществлена на слое DAO запросом к БД
     *
     * @param ownerId идентификатор пользователя владельца
     * @param item обрабатываемы в Service-слое Item-объект
     */
    private void assignItemWithOwner(Long ownerId, Item item) {
        userService.getById(ownerId); //если пользователь не найден -> NotFoundException
        item.setOwnerId(ownerId);
    }

    /**
     * ТЗ-13
     * <p> вспомогательный метод проверки принадлежности вещи пользователю
     * <p> реализация для in-memory репозитория
     * <p> при реалзиации репозитория в БД проверка может быть осуществлена на слое DAO запросом к БД
     *
     * @param ownerId идентификатор пользователя владельца
     * @param itemOwnerId идентификатор пользователя-владельца Item-объекта из репозитория
     */
    private void checkModificationAccess(Long ownerId, Long itemOwnerId) {
        if (!ownerId.equals(itemOwnerId)) {
            log.info("Error: requesting user not match item owner");
            throw new ForbiddenException("requesting user not match item owner") ;
        }
    }
}