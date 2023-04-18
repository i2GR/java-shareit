package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserRepositoryTest {

    private final UserRepository userStorage;

    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void reinitialiseUsers() {
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void createAndRead() {
        User expected = User.builder().name("name").email("email@host.dom").build();

        //when
        userStorage.save(expected);
        long count = userStorage.findAll().stream().map(User::getId).max(Long::compareTo).orElseThrow();

        //then
        User actual = userStorage.findById(count).orElseThrow(() -> new NotFoundException("not found"));

        assertThat(actual)
                .isNotNull()
                .hasNoNullFieldsOrProperties();
        assertEquals(expected, actual);
    }

    @Test
    void createDuplicatesShouldThrowException() {
        User user = User.builder().name("name").email("email@host.dom").build();
        User duplicateEmail = User.builder().name("new name").email("email@host.dom").build();
        User duplicateName = User.builder().name("new name").email("email@host.dom").build();

        //when
        userStorage.save(user);

        //then
        assertThrows(Throwable.class, () -> userStorage.save(duplicateEmail));
        assertThrows(Throwable.class, () -> userStorage.save(duplicateName));
    }
}