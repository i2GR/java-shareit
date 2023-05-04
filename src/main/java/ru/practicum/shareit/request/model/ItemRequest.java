package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.booking.validation.OnCreate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Model-класс информации о запросе вещи <p>
 * ТЗ-15 <p>
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "item request description cannot be blank")
    @Column
    private String description;

    /**
     * запросивший пользователь - существующий в ShareIt
     */
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    //TODO 15
    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "requests")
    //@Transient
    Set<Item> items = new HashSet<>();

    @Column
    private LocalDateTime created;

    public void addItem(Item item) {
        items.add(item);
        item.getRequests().add(this);
    }
}