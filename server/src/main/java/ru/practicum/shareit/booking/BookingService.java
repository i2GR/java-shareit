package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;


import java.util.List;

/**
 * интерфейс сервис-слой для обработки запросов на бронирование <p>
 * ТЗ-14 <p>
 * CRUD-функционал, подтверждение/отклонение бронирования
 */
public interface BookingService {

    /**
     * добавление запроса на бронирование
     * @param bookerId идентификатор пользователя, которому принадлежит вещь
     * @param dto DTO запроса на бронирование
     * @return экз. DTO для добавленного запроса на бронирование
     */
    BookingResponseDto addBooking(Long bookerId, BookingDto dto);

    /**
     * подтверждение или отклонение запроса на бронирование <p>
     * Может быть выполнено только владельцем вещи
     * статус бронирования становится либо APPROVED, либо REJECTED
     * @param ownerId идентификатор владельца вещи (X-sharer-user)
     * @param bookingId идентификатор запроса на бронирование
     * @param approveState значения true или false (подтверждено / отклонено)
     * @return DTO для запроса на бронирование
     */
    BookingResponseDto approve(Long ownerId, Long bookingId, Boolean approveState);

    /**
     * Получение данных о конкретном бронировании <p>
     * Может быть выполнено либо автором бронирования, либо владельцем вещи, к которой относится бронирование
     * @param userId идентификатор автора бронирования, либо владельца вещи
     * @param bookingId идентификатор бронирования
     * @return DTO для запроса на бронирование (включая его статус)
     */
    BookingResponseDto getByRelatedUserId(Long userId, Long bookingId);

    /**
     * получение списка всех бронирований текущего пользователя
     * @param bookerId идентификатор <b>АВТОРА бронирования</b>
     * @param status необязательный (по умолчанию равен ALL)
     * @param from индекс первого элемента (нумерация начинается с 0)
     * @param size количество элементов для отображения
     * @return DTO для запроса на бронирование (включая его статус)
     */
    List<BookingResponseDto> getListByBooker(Long bookerId, BookingStatus status, Long from, Integer size);

    /**
     * получение списка бронирований для всех вещей текущего пользователя
     * @param ownerId идентификатор <b>ВЛАДЕЛЬЦА вещи</b>
     * @param status необязательный (по умолчанию равен ALL)
     * @param from индекс первого элемента (нумерация начинается с 0)
     * @param size количество элементов для отображения
     * @return DTO для запроса на бронирование (включая его статус)
     */
    List<BookingResponseDto> getListByOwner(Long ownerId, BookingStatus status, Long from, Integer size);


    /**
     * удаление бронирования <p>
     * @implNote если вещь не принадлежит пользователю, она не должна быть удалена
     * @implNote ендпойнт не определен в ТЗ-14
     * @param ownerId идентификатор пользователя, которому принадлежит вещь
     * @param itemId идентификатор сохраненной вещи
     * @return DTO для пользователя (уудаленный пользователь)
     */
    String deleteById(Long ownerId, Long itemId);
}
