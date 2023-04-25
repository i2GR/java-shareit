package ru.practicum.shareit.booking;

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
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * сортировка по убыванию времени начала бронирования
     * @param bookerId идентификатор пользователя-заказчика
     * @return список (List)
     */
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * выборка по времени начала бронирования после текущего момента <p>
     * сортировка по убыванию времени начала бронирования
     * @param bookerId идентификатор пользователя-заказчика
     * @param moment текущий момент
     * @return список (List)
     */
    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime moment);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * выборка по времени окончания бронирования ДО текущего момента <p>
     * сортировка по убыванию времени начала бронирования
     * @param bookerId идентификатор пользователя-заказчика
     * @param moment текущий момент
     * @return список (List)
     */
    List<Booking> findByBookerIdAndEndIsBeforeOrderByEndDesc(Long bookerId, LocalDateTime moment);

    /**
     * получение списка всех бронирований пользователя-владельца <p>
     * сортировка по убыванию времени начала бронирования
     * @param ownerId дентификатор пользователя-владельца
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdOrderByStartDesc(Long ownerId);

    /**
     * получение <b>ПОСЛЕДНЕГО бронирования</b><p>
     * выборка по времени начала бронирования до текущего момента <p>
     * сортировка по убыванию времени окончания бронирования
     * @param itemId идентификатор вещи
     * @param moment текущий момент
     * @param statusList статусы бронирования, которые исключаются из поиска
     * @return информация о бронировании ,соответствующего условиям поиска
     */
    Optional<Booking> findFirst1ByItemIdAndStartLessThanEqualAndStatusNotInOrderByStartDesc(
            Long itemId,
            LocalDateTime moment,
            List<BookingStatus> statusList);

    /**
     * получение <b>СЛЕДУЮЩЕГО бронирования</b><p>
     * выборка по времени начала бронирования после текущего момента <p>
     * сортировка по возрастанию времени окончания бронирования
     * @param itemId идентификатор вещи
     * @param moment текущий момент
     * @param statusList статусы бронирования, которые исключаются из поиска
     * @return список (List)
     */
    Optional<Booking> findFirst1ByItemIdAndStartGreaterThanEqualAndStatusNotInOrderByStartAsc(
            Long itemId,
            LocalDateTime moment,
            List<BookingStatus> statusList);

    /**
     * получение списка бронирований пользователя-владельца <p>
     * <b>для выдачи списка БУДУЩИХ бронирований</b> <p>
     * выборка по времени начала бронирования после текущего момента <p>
     * сортировка по убывванию времени окончания бронирования
     * @param moment текущий момент
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime moment);

    /**
     * получение списка бронирований пользователя-владельца <p>
     * <b>для выдачи списка ПРОШЛЫХ бронирований</b> <p>
     * выборка по времени окончаниябронирования до текущего момента <p>
     * сортировка по убывванию времени окончания бронирования
     * @param moment текущий момент
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(Long ownerId, LocalDateTime moment);

    /**
     * получение списка бронирований пользователя-заказчика <p>
     * <b>для выдачи списка ТЕКУЩИХ бронирований</b> <p>
     * выборка по времени начала ДО и времени окончания ПОСЛЕ текущего момента <p>
     * сортировка по убывванию времени окончания бронирования
     * @param ownerId идентификатор пользователя-заказчика
     * @param forStart текущий момент
     * @param forEnd текущий момент
     * @return список (List)
     */
    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Long ownerId,
                                                                            LocalDateTime forStart,
                                                                            LocalDateTime forEnd);

    /**
     * получение списка бронирований пользователя-владельца <p>
     * выборка по Статусу <p>
     * сортировка по убывванию времени начала бронирования
     * @param ownerId идентификатор пользователя-заказчика
     * @param status статус для выборки
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);

    /**
     * получение любого бронирования пользователя-заказчика <p>
     *  для проверки факта бронирования в прошлом при добавлении комментария к вещи <p>
     * @param bookerId идентификатор пользователя-заказчика
     * @param itemId идентификатор вещи
     * @param localDateTime текущий момент времени
     * @param bookingStatus подтвежденный статус бронирования (передается BookingStatus.APPROVED);
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
     * сортировка по убывванию времени окончания бронирования
     * @param ownerId идентификатор пользователя-заказчика
     * @param forStart текущий момент
     * @param forEnd текущий момент
     * @return список (List)
     */
    List<Booking> findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(Long ownerId,
                                                                                LocalDateTime forStart,
                                                                                LocalDateTime forEnd);
}