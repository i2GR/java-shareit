package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import static java.util.stream.Collectors.toList;
import static java.lang.String.format;

import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

/**
 * сервис-слой для обработки запросов на бронирование <p>
 * ТЗ-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingDtoMapper bookingMapper;

    private final BookingRepository bookingStorage;

    private final UserRepository userStorage;

    private final ItemRepository itemStorage;

    /**
     * добавление бронирования<p>
     * - проверка пользователя в БД<p>
     * - проверка вещи в БД <p>
     * - проверка доступности вещи<p>
     * - создание бронирования<p>
     * @param bookerId идентификатор пользователя-заказчика
     * @param dto DTO запроса на бронирование
     * @return DTO информация о бронировании
     */
    @Transactional
    @Override
    public BookingResponseDto addBooking(Long bookerId, BookingDto dto) {
        User booker = userStorage.findById(bookerId).orElseThrow(
                () -> {
                    log.info("User with id {} not found", bookerId);
                    throw new NotFoundException(format("user with id %d not found", bookerId));
                }
        );
        Long itemId = dto.getItemId();
        Item item = itemStorage.findById(itemId).orElseThrow(
                () -> {
                    log.info("Item with id {} not found", itemId);
                    throw new NotFoundException(format("item with id %d not found", itemId));
                }
        );
        if (bookerId.equals(item.getOwnerId())) {
            log.info("Booker user is item-owner user");
            throw new NotFoundException("Booker user is item-owner user");
        }
        if (item.getAvailable()) {
            Booking booking = bookingMapper.fromDto(dto, booker, item);
            Booking created = bookingStorage.save(booking);
            log.info("New booking added with new id {}", created.getId());
            return bookingMapper.toDto(created);
        }
        log.info("Error creating booking");
        throw new BadRequestException("Error creating booking");
    }

    /**
     * подтверждение бронирования<p>
     * - получение бронирования <p>
     * - проверка владельца вещи <p>
     * - проверка доступности вещи<p>
     * - подтверждение / отклонение бронирования<p>
     * @param ownerId идентификатор пользователя-владельца
     * @param bookingId идентификатор запроса на бронирование
     * @param approveState новый статус бронирования
     * @return DTO информация о бронировании
     */
    @Transactional
    @Override
    public BookingResponseDto approve(Long ownerId, Long bookingId, Boolean approveState) {
        Booking booking = readById(bookingId);
        if (booking.getStatus() != BookingStatus.WAITING) {
            log.info("bad status of booking {}", booking.getStatus().toString());
            throw new BadRequestException(format("bad status of booking %s", booking.getStatus().toString()));
        }
        if (ownerId.equals(booking.getItem().getOwnerId())) {
            booking.setStatus(approveState ? BookingStatus.APPROVED : BookingStatus.REJECTED);
            return bookingMapper.toDto(booking);
        }
        log.info("bad request of user {}", ownerId);
        throw new NotFoundException(format("bad request of user %d", ownerId));
    }

    /**
     * получение информации о бронировании пользователем владельцем/заказчиком
     * @param userId идентификатор автора бронирования, либо владельца вещи
     * @param bookingId идентификатор бронирования
     * @return DTO информация о бронировании
     */
    @Override
    public BookingResponseDto getByRelatedUserId(Long userId, Long bookingId) {
        Booking booking = readById(bookingId);
        if (userId.equals(booking.getBooker().getId()) ||
            userId.equals(booking.getItem().getOwnerId())) {
            log.info("booking id {} found", bookingId);
            return bookingMapper.toDto(booking);
        }
        log.info("User with id {} is not related to booking {}", userId, bookingId);
        throw new NotFoundException(format("User with id %d is not related to booking", userId));
    }

    /**
     * получение списка бронирований пользователя-заказчика
     * @param bookerId идентификатор <b>АВТОРА бронирования</b>
     * @param status необязательный (по умолчанию равен ALL)
     * @param from индекс первого элемента (нумерация начинается с 0)
     * @param size количество элементов для отображения
     * @return список DTO о бронировании
     */
    @Override
    public List<BookingResponseDto> getListByBooker(Long bookerId, BookingStatus status, Long from, Integer size) {
        checkUserExistsElseThrow(bookerId);
        LocalDateTime moment = LocalDateTime.now();
        switch (status) {
            case ALL:
                return listBookingResponseDTOs(
                        bookingStorage.findByBookerIdOrderByStartDesc(
                                bookerId, PageRequest.of((int) (from / size), size)));
            case FUTURE:
                return listBookingResponseDTOs(
                        bookingStorage.findByBookerIdAndStartIsAfterOrderByStartDesc(
                                bookerId, moment, PageRequest.of((int) (from / size), size)));
            case PAST:
                return listBookingResponseDTOs(
                        bookingStorage.findByBookerIdAndEndIsBeforeOrderByEndDesc(
                                bookerId, moment, PageRequest.of((int) (from / size), size)));
            case CURRENT:
                return listBookingResponseDTOs(
                        bookingStorage.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                                bookerId, moment, moment, PageRequest.of((int) (from / size), size)));
            //WAITING, REJECTED, CANCELLED
            default:
                return listBookingResponseDTOs(
                        bookingStorage.findByBookerIdAndStatusOrderByStartDesc(
                                bookerId, status, PageRequest.of((int) (from / size), size)));
        }
    }

    /**
     * получение списка бронирований пользователя-владельца
     * @param ownerId идентификатор <b>ВЛАДЕЛЬЦА вещи</b>
     * @param status необязательный (по умолчанию равен ALL)
     * @param from индекс первого элемента (нумерация начинается с 0)
     * @param size количество элементов для отображения
     * @return список DTO о бронировании
     */
    @Override
    public List<BookingResponseDto> getListByOwner(Long ownerId, BookingStatus status, Long from, Integer size) {
        checkUserExistsElseThrow(ownerId);
        LocalDateTime moment = LocalDateTime.now();
        switch (status) {
            case ALL:
                return listBookingResponseDTOs(
                        bookingStorage.findByItem_OwnerIdOrderByStartDesc(
                                ownerId, PageRequest.of((int) (from / size), size)));
            case FUTURE:
                return listBookingResponseDTOs(
                        bookingStorage.findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(
                                ownerId, moment, PageRequest.of((int) (from / size), size)));
            case PAST:
                return listBookingResponseDTOs(
                        bookingStorage.findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(
                                ownerId, moment, PageRequest.of((int) (from / size), size)));
            case CURRENT:
                return listBookingResponseDTOs(
                        bookingStorage.findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                                ownerId, moment, moment, PageRequest.of((int) (from / size), size)));
            //WAITING, REJECTED, CANCELLED
            default:
                return listBookingResponseDTOs(
                        bookingStorage.findByItem_OwnerIdAndStatusOrderByStartDesc(
                                ownerId, status, PageRequest.of((int) (from / size), size)));
        }
    }

    /**
     * удаление бронирования
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param bookingId идентификатор сохраненной вещи
     * @return сообщение об удалении
     */
    @Transactional
    @Override
    public String deleteById(Long ownerId, Long bookingId) {
        Booking booking = readById(bookingId);
        if (booking.getItem().getOwnerId().equals(ownerId)) {
            log.info("deleted booking with id {}", bookingId);
            return SUCCESS_DELETE_MESSAGE;
        }
        log.info("User with id {} is not related to booking", ownerId);
        throw new ForbiddenException(format("User with id %d is not related to booking", ownerId));
    }

    /**
     * вспомогательный метод получения запроса на бронирование из БД по идентификатору
     * При отсутствии в БД записи выбрасывает исключение приложения NotFoundException
     * @param bookingId идентификатор запроса на бронирование
     * @return экземпляр запроса на бронирование
     */
    private Booking readById(Long bookingId) {
        return bookingStorage.findById(bookingId).orElseThrow(
                () -> {
                    log.info("Booking with Id {} not found", bookingId);
                    throw new NotFoundException(format("Booking with Id %d not found", bookingId));
                }
        );
    }

    /**
     * вспомогательный метод проверки, что пользователь существует в ShareIt
     * @param userId идентификатор пользователя
     */
    private void checkUserExistsElseThrow(Long userId) {
        if (!userStorage.existsById(userId)) {
            log.info("user with id {} not found", userId);
            throw new NotFoundException(format("User with id %d not found", userId));
        }
    }

     /**
     * преобразование списка Booking в список BookingResponseDto
     * @param bookings List<Booking> bookings
     * @return List<BookingResponseDto>
     */
    private List<BookingResponseDto> listBookingResponseDTOs(List<Booking> bookings) {
        return bookings.stream().map(bookingMapper::toDto).collect(toList());
    }
}