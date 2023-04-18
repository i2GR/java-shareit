package ru.practicum.shareit.booking.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.ShareItEntity;

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
@EqualsAndHashCode(exclude = {"id"}, callSuper = false)
@Entity
@Table(name = "bookings")
public class Booking extends ShareItEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "booking start time is null")
    @Column(name = "start_date")
    private LocalDateTime start;

    @NotNull(message = "booking end time is null")
    @Column(name = "end_date")
    private LocalDateTime end;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @OneToOne
    @JoinColumn(name = "booker_id")
    private User booker;

    /**
     * статус может меняться во время работы приложения
     */
    @Builder.Default
    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    BookingStatus status = BookingStatus.WAITING;
}