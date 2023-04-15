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
import ru.practicum.shareit.user.UserRepository;
import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

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

    private final ItemDtoMapper itemMapper;

    @NonNull
    private final ItemRepository itemStorage;

    @NonNull
    private final UserRepository userStorage;

    @Override
    public ItemDto addItem(Long ownerId, ItemDto dto) {
        Item item = itemMapper.fromDto(dto);
        assignItemWithOwner(ownerId, item);
        Item created = itemStorage.create(item).orElseThrow(
                                                    () -> {
                                                        log.info("Service error creating Item");
                                                        throw new StorageErrorException("Service error creating Item"); }
                                                    );
        return itemMapper.toDto(created);
    }

    @Override
    public ItemDto patch(Long ownerId, Long itemId, ItemDto dto) {
        Item item = itemStorage.readById(itemId).orElseThrow(
                                                     () -> {
                                                         log.info("Service error reading Item#id {}", itemId);
                                                         throw new StorageErrorException(
                                                         String.format("Service error reading Item#id %d", itemId)); }
                                                     );
        checkUserAccess(ownerId, item.getOwnerId());
        itemMapper.update(dto, item);
        itemStorage.update(item).orElseThrow(
                                                    () -> {
                                                        log.info("Service error patching Item");
                                                        throw new StorageErrorException("Service error creating Item"); }
                                                    );
        return itemMapper.toDto(item);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemStorage.readById(itemId).orElseThrow(
                                                    () -> {
                                                        log.info("Service error reading Item#id {}", itemId);
                                                        throw new StorageErrorException(
                                                        String.format("Service error reading Item#id %d", itemId)); }
        );
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllByUserId(Long userId) {
        //реализация для in-memory репозитория
        return itemStorage.readAll().stream()
                                    .filter(i -> i.getOwnerId().equals(userId))
                                    .map(itemMapper::toDto)
                                    .collect(Collectors.toList());
    }

    @Override
    public String deleteById(Long ownerId, Long itemId) {
        Item item = itemStorage.readById(itemId).orElseThrow(
                                                    () -> {
                                                        log.info("Service error reading Item#id {}", itemId);
                                                        throw new StorageErrorException(
                                                        String.format("Service error reading Item#id %d", itemId)); }
        );
        checkUserAccess(ownerId, item.getOwnerId());
        itemStorage.delete(itemId).orElseThrow(
                                                    () -> {
                                                        log.info("Service error deleting Item#id {}: null received",
                                                            itemId);
                                                        throw new ServiceException(
                                                        String.format("received null deleting Item#id %d", itemId)); }
        );
        return SUCCESS_DELETE_MESSAGE;
    }

    @Override
    public List<ItemDto> search(String query) {
        return itemStorage.findByQuery(query).stream()
                                            .map(itemMapper::toDto)
                                            .collect(Collectors.toList());
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
        userStorage.readById(ownerId); //если пользователь не найден -> NotFoundException
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
    private void checkUserAccess(Long ownerId, Long itemOwnerId) {
        if (!ownerId.equals(itemOwnerId)) {
            log.info("Error: requesting user not match item owner");
            throw new ForbiddenException("requesting user not match item owner");
        }
    }
}