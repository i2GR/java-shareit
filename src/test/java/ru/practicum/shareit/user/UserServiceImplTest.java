package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    @InjectMocks
    private final UserServiceImpl userService;

    @MockBean
    private final UserRepository userStorage;

    private final User user = User.builder().id(1L).name("user").email("mail@host.dom").build();

    private UserDto requestDto;
    private UserDto responseDto;

    @Test
    void addUser_whenDataOk_thenOk() {
        //given
        requestDto = UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
        Mockito.when(userStorage.save(any())).thenReturn(user);
        //when
        responseDto = userService.addUser(requestDto);
        //then
        assertThat(responseDto).isNotNull();
        assertEquals(user.getName(), responseDto.getName());
        assertEquals(user.getEmail(), responseDto.getEmail());
    }

    @Test
    void addUser_whenEmailUpdated_thenUpdatedData() {
        //given
        requestDto = UserDto.builder()
                .email("updated@host.dom")
                .build();
        Mockito.when(userStorage.findById(any())).thenReturn(Optional.of(user));
        //when
        responseDto = userService.patch(user.getId(), requestDto);
        //then
        assertThat(responseDto).isNotNull();
        assertEquals(user.getName(), responseDto.getName());
        assertEquals("updated@host.dom", responseDto.getEmail());
        assertEquals(user.getId(), responseDto.getId());
    }

    @Test
    void patchUser_whenNotFound_thenNotFound() {
        //given
        requestDto = UserDto.builder()
                .email("updated@host.dom")
                .build();
        Mockito.when(userStorage.findById(any())).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> userService.patch(user.getId(), requestDto)
        );
        //then
        assertEquals(format("User#id %d not found", user.getId()), nfe.getMessage());
    }

    @Test
    void getById_whenInputIdOk_thenOk() {
        //given
        Mockito.when(userStorage.findById(user.getId())).thenReturn(Optional.of(user));
        //when
        responseDto = userService.patch(user.getId(), requestDto);
        //then
        assertThat(responseDto).isNotNull();
        assertEquals(user.getName(), responseDto.getName());
        assertEquals(user.getEmail(), responseDto.getEmail());
        assertEquals(user.getId(), responseDto.getId());
    }

    @Test
    void getAll_thenOk() {
        //given
        Mockito.when(userStorage.findAll()).thenReturn(List.of(user));
        //when
        List<UserDto> list = userService.getAll();
        //then
        assertThat(list).isNotNull();
        assertEquals(1, list.size());
        assertEquals(user.getName(), list.get(0).getName());
        assertEquals(user.getEmail(),  list.get(0).getEmail());
        assertEquals(user.getId(),  list.get(0).getId());
    }
}