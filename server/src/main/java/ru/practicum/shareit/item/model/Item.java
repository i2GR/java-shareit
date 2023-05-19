package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.persistence.*;

/**
 * Model-класс информации о вещи для шаринга <p>
 * ТЗ-13
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * идентификатор владельца - User#id
     */
    @Column(name = "owner_id")
    private Long ownerId;

    private String name;

    private String description;

    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest request;

    @Transient
    private Booking lastBooking;

    @Transient
    private Booking nextBooking;
}