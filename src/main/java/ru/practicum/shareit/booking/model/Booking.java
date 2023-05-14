package ru.practicum.shareit.booking.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import ru.practicum.shareit.booking.validation.OnCreate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Model-класс информации о заспросе вещи <p>
 * ТЗ-13 <p>
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(groups = {OnCreate.class},
            message = "booking start time is null")
    @Column(name = "start_date")
    private LocalDateTime start;

    @NotNull(groups = {OnCreate.class},
            message = "booking end time is null")
    @Column(name = "end_date")
    private LocalDateTime end;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne
    @JoinColumn(name = "booker_id")
    private User booker;

    /**
     * статус может меняться во время работы приложения
     */
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private BookingStatus status = BookingStatus.WAITING;
}