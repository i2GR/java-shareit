package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.lang.String.format;

import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * сервис-слой для обработки данных о вещах для шаринга <p>
 * ТЗ-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemDtoMapper itemMapper;

    private final ItemResponseDtoMapper itemResponseMapper;

    private final CommentDtoMapper commentMapper;

    private final ItemRepository itemStorage;

    private final BookingRepository bookingStorage;

    private final CommentRepository commentStorage;

    private final UserRepository userStorage;

    private final ItemRequestRepository requestStorage;


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
        assignRequestToItem(dto.getRequestId(), item);
        Item created = itemStorage.save(item);
        return itemMapper.toDto(created);
    }

    /**
     * обновление информации о вещи владельцем-вещи
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
        return itemMapper.toDto(item);
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
            Booking last = bookingStorage.findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                    itemId,
                    moment,
                    BookingStatus.APPROVED).orElse(null);
            item.setLastBooking(last);
            Booking next = bookingStorage.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                    itemId,
                    moment,
                    BookingStatus.APPROVED).orElse(null);
            item.setNextBooking(next);
        }
        List<ItemResponseDto.CommentResponseDto> commentResponseDtoList = commentStorage.findByItem_Id(itemId)
                .stream()
                .map(commentMapper::toNestedDto)
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
    public List<ItemResponseDto> getAllByUserId(Long from, Integer size, Long ownerId) {
        List<Item> items = itemStorage.findByOwnerIdEquals(ownerId, PageRequest.of((int) (from / size), size));
        List<Booking> bookings = bookingStorage.findByItem_OwnerIdOrderByStartDesc(
                ownerId, PageRequest.of((int) (from / size), size));
        LocalDateTime moment = LocalDateTime.now();
        Map<Long, List<ItemResponseDto.CommentResponseDto>> itemIdToCommentDtoList = commentStorage.findByItem_OwnerIdEquals(ownerId)
                .stream()
                .collect(groupingBy(comment -> comment.getItem().getId(), mapping(commentMapper::toNestedDto, toList())));
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
     * поиск вещи по текстовому запросу в названии или описании вещи
     * @param query строковое представление запроса
     * @param from индекс первого элемента (нумерация начинается с 0)
     * @param size количество элементов для отображения
     * @return список вещей: DTO-представление для класса Item <b>без</b>дополнительных полей
     */
    @Override
    public List<ItemDto> search(String query, Long from, Integer size) {
        if (query.isBlank()) {
            log.info("search query is blank");
            return List.of();
        }
        return itemStorage.findDistinctByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(query, query,
                        PageRequest.of((int) (from / size), size))
                            .stream()
                            .map(itemMapper::toDto)
                            .collect(toList());
    }

    /**
     * добавление комментария к вещи<p>
     * доступно только пользователю-заказчику с подтвержденным бронированием <p>
     * @param authorId идентификатор пользователя-автора комментария
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
                            return new BadRequestException("Booking by user of Item not exists");
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
     *  вспомогательный метод проверки принадлежности вещи пользователю <p>
     *  при реализации репозитория в БД проверка может быть осуществлена на слое DAO запросом к БД <p>
     * @param ownerId идентификатор пользователя владельца
     * @param item обрабатываемы в Service-слое Item-объект
     */
    private void assignItemWithOwner(Long ownerId, Item item) {
        userStorage.findById(ownerId).orElseThrow(
                    () -> {
                        log.info("User with Id {} not found", ownerId);
                        return new NotFoundException(format("User with Id %d not found", ownerId));
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
                    return new NotFoundException(String.format("Item with Id %d not found", itemId));
                }
        );
    }

    /**
     * вспомогательный метод ассоциации <b>последних</b> запросов на бронирование к вещам из списка<p>
     * при получении списка вещей по идентификатору пользователем-<b>владельцем</b><p><p>
     * @param items список вещей пользователя-владельца, к которым ассоциируется последний запрос
     * @param bookings список запросов на бронирование к вещам пользователя-владельца
     * @param moment текущий момент поиска
     */
    private void setLastBookingsToItems(List<Item> items, List<Booking> bookings, LocalDateTime moment) {
        Map<Long, Booking> itemIdMapsLastBooking = bookings.stream()
                .filter(b -> !b.getStart().isAfter(moment))
                .collect(toMap(b -> b.getItem().getId(), identity(), (o, n) -> o));
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
        Map<Long, Booking> itemIdMapsNextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(moment))
                .collect(toMap(b -> b.getItem().getId(), identity(), (o, n) -> n));
        items.forEach(item -> item.setNextBooking(itemIdMapsNextBooking.get(item.getId())));
    }

    /**
     * вспомогательный метод привязки вещи пользователю к запросу (ItemRequest) <p>
     * ТЗ-15 <p>
     * @param requestId идентификатор запроса на вещь, в ответ на который создается вещь
     * @param item обрабатываемый в Service-слое Item-объект
     */
    private void assignRequestToItem(Long requestId, Item item) {
        if (requestId == null) {
            log.info("RequestId is null");
            return;
        }
        ItemRequest request = requestStorage.findById(requestId).orElse(null);
        if (request == null) {
            log.info("Item-Request with Id {} not found", requestId);
            return;
        }
        item.setRequest(request);
    }
}