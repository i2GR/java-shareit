package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
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

import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * сервис-слой для обработки данных вещах для шаринга <p>
 * ТЗ-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService implements ItemServing {

    @NonNull
    private final ItemDtoMapper itemMapper;

    @NonNull
    private final ItemResponseDtoMapper itemResponseMapper;

    @NonNull
    private final CommentDtoMapper commentMapper;

    @NonNull
    private final ItemRepository itemStorage;

    @NonNull
    private final BookingRepository bookingStorage;

    @NonNull
    private final CommentRepository commentStorage;

    @NonNull
    private final UserRepository userStorage;

    @Override
    public ItemDto addItem(Long ownerId, ItemDto dto) {
        Item item = itemMapper.fromDto(dto);
        assignItemWithOwner(ownerId, item);
        Item created = itemStorage.save(item);
        return itemMapper.toDto(created);
    }

    @Override
    public ItemDto patch(Long ownerId, Long itemId, ItemDto dto) {
        Item item = readById(itemId);
        checkUserAccess(ownerId, item.getOwnerId());
        itemMapper.update(dto, item);
        Item updated = itemStorage.save(item);
        return itemMapper.toDto(updated);
    }

    @Override
    public ItemResponseDto getById(Long itemId) {
        return itemResponseMapper.toDto(readById(itemId));
    }

    @Override
    public ItemResponseDto getByOwnerById(Long userId, Long itemId) {
        Item item = readById(itemId);
        if (userId.equals(item.getOwnerId())) {
            LocalDateTime moment = LocalDateTime.now(); //
            Booking last = bookingStorage.findByItem_OwnerIdAndStartIsBeforeOrderByEndDesc(userId, moment)
                    .stream()
                    .filter(b -> (b.getStatus() != BookingStatus.WAITING && b.getStatus() != BookingStatus.REJECTED))
                    .max((b1, b2) -> b1.getEnd().isBefore(b2.getEnd()) ? -1 : 1)
                    .orElse(null);
            item.setLastBooking(last);
            Booking next = bookingStorage.findByItem_OwnerIdAndStartIsAfterOrderByStartAsc(userId, moment)
                    .stream()
                    .filter(b -> (b.getStatus() != BookingStatus.WAITING && b.getStatus() != BookingStatus.REJECTED))
                    .findFirst()
                    .orElse(null);
            item.setNextBooking(next);
            int a = 1;
        }
        List<CommentResponseDto> commentResponseDtoList = commentStorage.findByItem_Id(itemId)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        ItemResponseDto itemDto = itemResponseMapper.toDto(item);
        itemDto.setComments(commentResponseDtoList);
        return itemDto;
    }

    @Override
    public List<ItemResponseDto> getAllByUserId(Long ownerId) {
        List<Item> items = itemStorage.findByOwnerIdEquals(ownerId);
        List<Booking> bookings = bookingStorage.findByItem_OwnerIdOrderByStartDesc(ownerId);
        LocalDateTime moment = LocalDateTime.now();
        List<Comment> commentsOfUserItems = commentStorage.findByItem_OwnerIdEquals(ownerId);
        Map<Long, List<CommentResponseDto>> itemIdToCommentDtoList = mapItemIdToCommentDtoList(items, commentsOfUserItems);
        setLastBookingsToItems(items, bookings, moment);
        setNextBookingsToItems(items, bookings, moment);
        return items.stream()
                .map(itemResponseMapper::toDto)
                .peek(dto -> dto.setComments(itemIdToCommentDtoList.get(dto.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public String deleteById(Long ownerId, Long itemId) {
        checkUserAccess(ownerId, readById(itemId).getId());
        itemStorage.deleteById(itemId);
        log.info("deleted {} item ", itemId);
        return SUCCESS_DELETE_MESSAGE;
    }

    @Override
    public List<ItemDto> search(String query) {
        if (query == null || query.isBlank()) {
            log.info("search query is null or blank");
            return List.of();
        }
        return itemStorage.findDistinctByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(query, query).stream()
                                            .map(itemMapper::toDto)
                                            .collect(Collectors.toList());
    }

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
                        throw new NotFoundException(String.format("User with Id %d not found", ownerId));
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

    private Item readById(Long itemId) {
        return itemStorage.findById(itemId).orElseThrow(
                () -> {
                    log.info("Item with Id {} not found", itemId);
                    throw new NotFoundException(String.format("Item with Id %d not found", itemId));
                }
        );
    }

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

    private Map<Long, List<CommentResponseDto>> mapItemIdToCommentDtoList(List<Item> items, List<Comment> comments) {
        Map<Long, List<CommentResponseDto>> itemIdMapsListOfComments = new HashMap<>();
        for (Comment comment : comments) {
            itemIdMapsListOfComments.putIfAbsent(comment.getItem().getId(), new ArrayList<>()).add(commentMapper.toDto(comment));
        }
        return itemIdMapsListOfComments;
    }
}