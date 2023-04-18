package ru.practicum.shareit.user.model;

import lombok.*;
import ru.practicum.shareit.util.ShareItEntity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

/**
 * Model-класс информации о пользователе <p>
 * ТЗ-13
 */
@Builder
@Getter
@Setter
@AllArgsConstructor/*(access = AccessLevel.PACKAGE)*/
@NoArgsConstructor/*(access = AccessLevel.PACKAGE)*/
//@Setter/*(value = AccessLevel.PACKAGE)*/
@EqualsAndHashCode(exclude = {"id"}, callSuper = false)
@Entity
@Table(name = "users")
public class User extends ShareItEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //TODO delete?
    @Column(name = "id")
    private Long id;

    @Email(message = "Bad User.email")
    //TODO delete?
    @Column(name = "email", unique = true)
    private String email;

    @NotNull
    //TODO delete?
    @Column(name = "name")
    private String name;
}