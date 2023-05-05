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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @NotNull (message = "Owner is null")
    @Column(name = "owner_id")
    private Long ownerId;

    @NotNull (message = "item name is null")
    private String name;

    @NotBlank(message = "item description cannot be blank")
    private String description;

    @NotNull (message = "item available is null")
    private Boolean available;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private ItemRequest request;

    //TODO 15
    //@ManyToMany(fetch = FetchType.EAGER)
    //@JoinTable(
    //       name = "item_requests",
    //        joinColumns = @JoinColumn(name = "item_id"),
    //        inverseJoinColumns = @JoinColumn(name = "request_id"))
    //private Set<ItemRequest> requests = new HashSet<>();

    @Transient
    private Booking lastBooking;

    @Transient
    private Booking nextBooking;
/*    public void addItemRequest(ItemRequest itemRequest) {
        requests.add(itemRequest);
        itemRequest.getItems().add(this);
    }*/
}