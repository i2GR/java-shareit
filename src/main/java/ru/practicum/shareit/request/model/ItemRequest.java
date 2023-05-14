package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

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

    @Column
    private LocalDateTime created;
}