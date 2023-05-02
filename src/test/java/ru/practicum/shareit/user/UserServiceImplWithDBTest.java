package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.user.dto.UserDto;
import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplWithDBTest {

    private final UserServiceImpl userService;

    private final JdbcTemplate jdbcTemplate;

    private static final String DEFAULT_NAME = "name";
    private static final String DEFAULT_EMAIL = "mail@host.dom";
    private static final UserDto INITIAL = UserDto.builder().name(DEFAULT_NAME).email(DEFAULT_EMAIL).build();


    @BeforeEach
    void reinitialiseUsers() {
        jdbcTemplate.update("DELETE FROM bookings");
        jdbcTemplate.update("DELETE FROM items");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void addUserAndGetById() {
        UserDto expected = INITIAL;

        //when
        long assignedId = userService.addUser(expected).getId();
        UserDto actual = userService.getById(assignedId);

        //then
        assertThat(actual)
                .isNotNull()
                .hasNoNullFieldsOrProperties();
        assertEquals(expected, actual);
    }

    @Test
    void createDuplicatesShouldThrowException() {
        UserDto duplicateEmail = UserDto.builder().name("new name").email(DEFAULT_EMAIL).build();
        UserDto duplicateName = UserDto.builder().name(DEFAULT_NAME).email("new@host.dom").build();

        //when
        userService.addUser(INITIAL);

        //then
        assertThrows(Throwable.class, () -> userService.addUser(duplicateEmail));
        assertThat(userService.addUser(duplicateName))
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .isEqualTo(duplicateName);
    }

    @Test
    void patchNewNameOrEmail() {
        String updatedEmail = "updated@host.dom";
        String newName = "new name";
        UserDto userDtoNewEmail = UserDto.builder().email(updatedEmail).build();
        UserDto userDtoNewName = UserDto.builder().name(newName).build();

        //when
        long assignedId = userService.addUser(INITIAL).getId();

        //then
        assertThat(userService.patch(assignedId, userDtoNewEmail))
                .isNotNull()
                .hasNoNullFieldsOrProperties()
                .hasFieldOrPropertyWithValue("name", DEFAULT_NAME)
                .hasFieldOrPropertyWithValue("email", updatedEmail);
        assertThat(userService.patch(assignedId, userDtoNewName))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", newName)
                .hasFieldOrPropertyWithValue("email", updatedEmail);
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MAX_VALUE, 0L, -1L})
    void patchBadId(long id) {
        assertThrows(Throwable.class, () -> userService.patch(id, INITIAL));
    }

    @Test
    void getAll() {
        UserDto second = UserDto.builder().name("second").email("second@mail.dom").build();

        //when
        List<UserDto> empty = userService.getAll();
        userService.addUser(INITIAL);
        List<UserDto> listOfOne = userService.getAll();
        userService.addUser(second);
        List<UserDto> listOfTwo = userService.getAll();

        //then
        assertEquals(0, empty.size());
        assertEquals(1, listOfOne.size());
        assertEquals(2, listOfTwo.size());
        assertTrue(listOfOne.contains(INITIAL));
        assertTrue(listOfTwo.contains(INITIAL));
        assertTrue(listOfTwo.contains(second));
    }

    @Test
    void deleteById() {
        UserDto second = UserDto.builder().name("second").email("second@mail.dom").build();

        //when
        userService.addUser(INITIAL);
        long idToDelete = userService.addUser(second).getId();
        List<UserDto> listOfTwo = userService.getAll();
        String res = userService.deleteById(idToDelete);
        List<UserDto> remained = userService.getAll();

        //then
        assertEquals(2, listOfTwo.size());
        assertEquals(1, remained.size());
        assertEquals(SUCCESS_DELETE_MESSAGE, res);
        assertTrue(remained.contains(INITIAL));
        assertFalse(remained.contains(second));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MAX_VALUE, 0L, -1L})
    void deleteByBadId(long idToDelete) {
        assertThrows(BadRequestException.class, () -> userService.deleteById(idToDelete));
    }
}