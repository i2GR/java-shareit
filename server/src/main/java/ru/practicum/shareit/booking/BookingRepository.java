package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * интерфейс для Jpa-репозитория запросов на бронирование
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * фильтр по статусу <p>
     * сортировка по убыванию времени начала бронирования
     * @param bookerId идентификатор пользователя-заказчика
     * @param status статус для выборки
     * @return список (List)
     */
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * сортировка по убыванию времени начала бронирования
     * @param bookerId идентификатор пользователя-заказчика
     * @return список (List)
     */
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * выборка по времени начала бронирования после текущего момента <p>
     * сортировка по убыванию времени начала бронирования
     * @param bookerId идентификатор пользователя-заказчика
     * @param moment текущий момент
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime moment, Pageable pageable);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * выборка по времени окончания бронирования ДО текущего момента <p>
     * сортировка по убыванию времени начала бронирования
     * @param bookerId идентификатор пользователя-заказчика
     * @param moment текущий момент
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByBookerIdAndEndIsBeforeOrderByEndDesc(Long bookerId, LocalDateTime moment, Pageable pageable);

    /**
     * получение списка всех бронирований пользователя-владельца <p>
     * сортировка по убыванию времени начала бронирования
     * @param ownerId идентификатор пользователя-владельца
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    /**
     * получение <b>ПОСЛЕДНЕГО бронирования</b><p>
     * выборка по времени начала бронирования до текущего момента <p>
     * сортировка по убыванию времени окончания бронирования
     * @param itemId идентификатор вещи
     * @param moment текущий момент
     * @param status статус бронирования - APPROVED
     * @return информация о бронировании, соответствующего условиям поиска
     */
    Optional<Booking> findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
            Long itemId,
            LocalDateTime moment,
            BookingStatus status);

    /**
     * получение <b>СЛЕДУЮЩЕГО бронирования</b><p>
     * выборка по времени начала бронирования после текущего момента <p>
     * сортировка по возрастанию времени окончания бронирования
     * @param itemId идентификатор вещи
     * @param moment текущий момент
     * @param status статус бронирования APPROVED
     * @return список (List)
     */
    Optional<Booking> findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
            Long itemId,
            LocalDateTime moment,
            BookingStatus status);

    /**
     * получение списка бронирований пользователя-владельца <p>
     * <b>для выдачи списка БУДУЩИХ бронирований</b> <p>
     * выборка по времени начала бронирования после текущего момента <p>
     * сортировка по убыванию времени окончания бронирования
     * @param moment текущий момент
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime moment, Pageable pageable);

    /**
     * получение списка бронирований пользователя-владельца <p>
     * <b>для выдачи списка ПРОШЛЫХ бронирований</b> <p>
     * выборка по времени окончания бронирования до текущего момента <p>
     * сортировка по убыванию времени окончания бронирования
     * @param moment текущий момент
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(Long ownerId, LocalDateTime moment, Pageable pageable);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * <b>для выдачи списка ТЕКУЩИХ бронирований</b> <p>
     * выборка по времени начала ДО и времени окончания ПОСЛЕ текущего момента <p>
     * сортировка по убыванию времени окончания бронирования
     * @param ownerId идентификатор пользователя-заказчика
     * @param forStart текущий момент
     * @param forEnd текущий момент
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Long ownerId,
                                                                            LocalDateTime forStart,
                                                                            LocalDateTime forEnd,
                                                                            Pageable pageable);

    /**
     * получение списка бронирований пользователя-владельца <p>
     * выборка по Статусу <p>
     * сортировка по убыванию времени начала бронирования
     * @param ownerId идентификатор пользователя-заказчика
     * @param status статус для выборки
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status, Pageable pageable);

    /**
     * получение любого бронирования пользователя-заказчика <p>
     *  для проверки факта бронирования в прошлом при добавлении комментария к вещи <p>
     * @param bookerId идентификатор пользователя-заказчика
     * @param itemId идентификатор вещи
     * @param localDateTime текущий момент времени
     * @param bookingStatus подтвержденный статус бронирования (передается BookingStatus.APPROVED);
     * @return бронирование (если найдено)
     */
    Optional<Booking> findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(Long bookerId,
                                                                            Long itemId,
                                                                            LocalDateTime localDateTime,
                                                                            BookingStatus bookingStatus);

    /**
     * получение списка бронирований пользователя-владельца <p>
     * <b>для выдачи списка ТЕКУЩИХ бронирований</b> <p>
     * выборка по времени начала ДО и времени окончания ПОСЛЕ текущего момента <p>
     * сортировка по убыванию времени окончания бронирования
     * @param ownerId идентификатор пользователя-заказчика
     * @param forStart текущий момент
     * @param forEnd текущий момент
     * @param pageable параметр постраничного вывода
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Long ownerId,
                                                                                LocalDateTime forStart,
                                                                                LocalDateTime forEnd,
                                                                                Pageable pageable);
}