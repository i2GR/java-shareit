package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.groupingBy;
import static java.lang.String.format;

import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * сервис-слой для обработки данных вещах для шаринга <p>
 * ТЗ-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService implements ItemServing {

    private final ItemDtoMapper itemMapper;

    private final ItemResponseDtoMapper itemResponseMapper;

    private final CommentDtoMapper commentMapper;

    private final ItemRepository itemStorage;

    private final BookingRepository bookingStorage;

    private final CommentRepository commentStorage;

    private final UserRepository userStorage;


    /**
     * добавление вещи
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param dto DTO-представление вещи
     * @return простое DTO-представление для класса Item без дополнительных полей
     */
    @Transactional
    @Override
    public ItemDto addItem(Long ownerId, ItemDto dto) {
        Item item = itemMapper.fromDto(dto);
        assignItemWithOwner(ownerId, item);
        Item created = itemStorage.save(item);
        return itemMapper.toDto(created);
    }

    /**
     * обновление инфомарции о вещи владельцем-вещи
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param itemId идентификатор вещи, содержащейся в приложении
     * @param dto DTO для вещи <p>
     * частично заполненные поля
     * @return простое DTO-представление для класса Item без дополнительных полей
     */
    @Transactional
    @Override
    public ItemDto patch(Long ownerId, Long itemId, ItemDto dto) {
        Item item = readById(itemId);
        checkUserAccess(ownerId, item.getOwnerId());
        itemMapper.update(dto, item);
        Item updated = itemStorage.save(item);
        return itemMapper.toDto(updated);
    }

    /**
     * получение вещи по идентификатору пользователем-<b>владельцем</b><p>
     * @param ownerId идентификатор пользователя, сделавшего Http-запрос
     * @param itemId идентификатор сохраненной вещи
     * @return DTO-представление для класса Item <b>с</b>дополнительными полями <p>
     *     (запросы на бронирование, комментарии)
     */
    @Override
    public ItemResponseDto getByOwnerById(Long ownerId, Long itemId) {
        Item item = readById(itemId);
        if (ownerId.equals(item.getOwnerId())) {
            LocalDateTime moment = LocalDateTime.now();
            Booking last = bookingStorage.findFirst1ByItemIdAndStartLessThanEqualAndStatusNotInOrderByStartDesc(
                    itemId,
                    moment,
                    List.of(BookingStatus.WAITING, BookingStatus.REJECTED)).orElse(null);
            item.setLastBooking(last);
            Booking next = bookingStorage.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusNotInOrderByStartAsc(
                    itemId,
                    moment,
                    List.of(BookingStatus.WAITING, BookingStatus.REJECTED)).orElse(null);
            item.setNextBooking(next);
        }
        List<CommentResponseDto> commentResponseDtoList = commentStorage.findByItem_Id(itemId)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        ItemResponseDto itemDto = itemResponseMapper.toDto(item);
        itemDto.setComments(commentResponseDtoList);
        return itemDto;
    }

    /**
     * получение списка вещей по идентификатору пользователем-<b>владельцем</b><p>
     * @param ownerId идентификатор пользователя-владельца вещи
     * @return DTO-представление для класса Item <b>с</b>дополнительными полями <p>
     *     (запросы на бронирование, комментарии)
     */
    @Override
    public List<ItemResponseDto> getAllByUserId(Long ownerId) {
        List<Item> items = itemStorage.findByOwnerIdEquals(ownerId);
        List<Booking> bookings = bookingStorage.findByItem_OwnerIdOrderByStartDesc(ownerId);
        LocalDateTime moment = LocalDateTime.now();
        Map<Long, List<CommentResponseDto>> itemIdToCommentDtoList = commentStorage.findByItem_OwnerIdEquals(ownerId)
                .stream()
                .collect(groupingBy(comment -> comment.getItem().getId(), mapping(commentMapper::toDto, toList())));
        setLastBookingsToItems(items, bookings, moment);
        setNextBookingsToItems(items, bookings, moment);
        return items.stream()
                .map(itemResponseMapper::toDto)
                .peek(dto -> dto.setComments(itemIdToCommentDtoList.get(dto.getId())))
                .collect(Collectors.toList());
    }

    /**
     * удаление вещи по идентификатору пользователем-<b>владельцем</b><p>
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param itemId идентификатор сохраненной вещи
     * @implNote по ТЗ не определен эндпойнт
     * @return сообщение об удалении
     */
    @Transactional
    @Override
    public String deleteById(Long ownerId, Long itemId) {
        checkUserAccess(ownerId, readById(itemId).getId());
        itemStorage.deleteById(itemId);
        log.info("deleted {} item ", itemId);
        return SUCCESS_DELETE_MESSAGE;
    }

    /**
     * поиск вещи по текстовому запросу в названию или описании вещи
     * @param query строковое представление запроса
     * @return список вещей: DTO-представление для класса Item <b>без</b>дополнительных полей
     */
    @Override
    public List<ItemDto> search(String query) {
        if (query == null || query.isBlank()) {
            log.info("search query is null or blank");
            return List.of();
        }
        return itemStorage.findDistinctByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(query, query).stream()
                                            .map(itemMapper::toDto)
                                            .collect(toList());
    }

    /**
     * добавление комментария к вещи<p>
     * доступно только пользователю-заказчику с подтвержденным бронированием <p>
     * @param authorId идентификатор пользователя- автора комментария
     * @param itemId идентификатор вещи для шаринга
     * @param commentDto DTO-представление комментария
     * @return DTO-представление для класса Item <b>с</b>дополнительными полями <p>
     *      (запросы на бронирование, комментарии)
     */
    @Transactional
    @Override
    public CommentResponseDto addComment(Long authorId, Long itemId, CommentDto commentDto) {
        LocalDateTime instant = LocalDateTime.now();
        Booking booking = bookingStorage.findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(
                                                        authorId, itemId, instant, BookingStatus.APPROVED)
                .orElseThrow(
                        () -> {
                            log.info("Booking by user {} of Item {} not exists", authorId, itemId);
                            throw new BadRequestException("Booking by user of Item not exists");
                        }
        );
        Item item = booking.getItem();
        User author = booking.getBooker();
        Comment comment = commentMapper.fromDto(commentDto, author, item, instant);
        commentStorage.save(comment);
        return commentMapper.toDto(comment);
    }

    /**
     * ТЗ-13
     * <p> вспомогательный метод проверки принадлежности вещи пользователю
     * <p> реализация для in-memory репозитория
     * <p> при реализации репозитория в БД проверка может быть осуществлена на слое DAO запросом к БД
     *
     * @param ownerId идентификатор пользователя владельца
     * @param item обрабатываемы в Service-слое Item-объект
     */
    private void assignItemWithOwner(Long ownerId, Item item) {
        userStorage.findById(ownerId).orElseThrow(
                    () -> {
                        log.info("User with Id {} not found", ownerId);
                        throw new NotFoundException(format("User with Id %d not found", ownerId));
                    }
        );
        item.setOwnerId(ownerId);
    }

    /**
     * ТЗ-13
     * вспомогательный метод проверки принадлежности вещи пользователю <p>
     * @param ownerId идентификатор пользователя владельца
     * @param itemOwnerId идентификатор пользователя-владельца Item-объекта из репозитория
     */
    private void checkUserAccess(Long ownerId, Long itemOwnerId) {
        if (!ownerId.equals(itemOwnerId)) {
            log.info("Error: requesting user not match item owner");
            throw new ForbiddenException("requesting user not match item owner");
        }
    }

    /**
     * вспомогательный метод чтения вещи из БД
     * @param itemId идентификатор вещи
     * @return сущность вещи, если она найдена в базе
     */

    private Item readById(Long itemId) {
        return itemStorage.findById(itemId).orElseThrow(
                () -> {
                    log.info("Item with Id {} not found", itemId);
                    throw new NotFoundException(String.format("Item with Id %d not found", itemId));
                }
        );
    }

    /**
     * вспомогательный метод ассоциации <b>последних</b> запросов на бронирование к вещам из списка<p>
     * при получении списка вещей по идентификатору пользователем-<b>владельцем</b><p><p>
     * @param items список вещей пользователя-владельца,к которым ассоциируется последний запрос
     * @param bookings список запросов на бронирование к вещам пользователя-владельца
     * @param moment текущий момент поиска
     */
    private void setLastBookingsToItems(List<Item> items, List<Booking> bookings, LocalDateTime moment) {
        Map<Long, Booking> itemIdMapsLastBooking = new HashMap<>();
        for (Booking booking: bookings) {
            if (booking.getEnd().isBefore(moment)) {
                itemIdMapsLastBooking.computeIfPresent(booking.getItem().getId(),
                        (l, b) -> b.getEnd().isAfter(booking.getEnd()) ? booking : b);
                itemIdMapsLastBooking.putIfAbsent(booking.getItem().getId(), booking);
            }
        }
        items.forEach(item -> item.setLastBooking(itemIdMapsLastBooking.get(item.getId())));
    }

    /**
     * вспомогательный метод ассоциации <b>следующих</b> запросов на бронирование к вещам из списка<p>
     * при получении списка вещей по идентификатору пользователем-<b>владельцем</b><p><p>
     * @param items список вещей пользователя-владельца ассоциируется следующий запрос
     * @param bookings список запросов на бронирование к вещам пользователя-владельца
     * @param moment текущий момент поиска
     */
    private void setNextBookingsToItems(List<Item> items, List<Booking> bookings, LocalDateTime moment) {
        Map<Long, Booking> itemIdMapsNextBooking = new HashMap<>();
        for (Booking booking: bookings) {
            if (booking.getStart().isAfter(moment)) {
                itemIdMapsNextBooking.computeIfPresent(booking.getItem().getId(),
                        (l, b) -> b.getStart().isBefore(booking.getStart()) ? b : booking);
                itemIdMapsNextBooking.putIfAbsent(booking.getItem().getId(), booking);
            }
        }
        items.forEach(item -> item.setNextBooking(itemIdMapsNextBooking.get(item.getId())));
    }
}