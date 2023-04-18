package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.util.ShareItEntity;
import ru.practicum.shareit.request.model.ItemRequest;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Model-класс информации о вещи для шаринга <p>
 * ТЗ-13
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id"}, callSuper = false)
@Entity
@Table(name = "items")
public class Item extends ShareItEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //TODO delete?
    @Column(name = "id")
    private Long id;

    /**
     * идентификатор владельца - User#id
     */
    @NotNull (message = "Owner is null")
    @Column(name = "owner_id")
    private Long ownerId;

    @NotNull (message = "item name is null")
    //TODO delete?
    @Column(name = "name")
    private String name;

    @NotBlank(message = "item description cannot be blank")
    //TODO delete?
    @Column(name = "description")
    private String description;

    @NotNull (message = "item available is null")
    //TODO delete?
    @Column(name = "available")
    private Boolean available;

    @Transient
    private ItemRequest request;

    @Transient
    Booking lastBooking;

    @Transient
    Booking nextBooking;
}