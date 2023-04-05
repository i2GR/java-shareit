package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDTOMapperTest {

    @Autowired
    private UserDtoMapper mapper;
    @Test
    void toDto() {
        User user = User.builder().id(1L).email("email@host.com").name("name").build();

        UserDto userDto = mapper.toDto(user);

        assertNotNull(userDto);
        assertEquals("email@host.com", userDto.getEmail());
        assertEquals("name", userDto.getName());
    }

    @Test
    void fromDto() {
        UserDto userDto = UserDto.builder().id(1L).email("email@host.dom").name("email@host.dom").build();

        User user = mapper.fromDto(userDto);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("email@host.dom", user.getEmail());
        assertEquals("email@host.dom", user.getName());
    }

    @Test
    void update() {
        User userToUpdateEmail = User.builder().id(1L).email("email@host.dom").name("name").build();
        User userToUpdateName = User.builder().id(1L).email("email@host.dom").name("name").build();

        UserDto userDtoEmailOnly = UserDto.builder().email("newmail@host.dom").build();
        UserDto userDtoNameOnly = UserDto.builder().name("new name").build();

        mapper.update(userDtoEmailOnly, userToUpdateEmail);
        mapper.update(userDtoNameOnly, userToUpdateName);


        assertEquals("newmail@host.dom", userToUpdateEmail.getEmail());
        assertEquals("name", userToUpdateEmail.getName());

        assertEquals("email@host.dom", userToUpdateName.getEmail());
        assertEquals("new name", userToUpdateName.getName());
    }
}