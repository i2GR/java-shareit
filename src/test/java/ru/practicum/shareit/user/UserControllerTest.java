package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    private static final String PATH = "/users";

    @MockBean
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;

    private final Long userId = 1L;
    private final String name = "name";
    private final String email = "email@host.dom";

    private UserDto userDto;

    @Test
    void postUser_whenInputDataOk_thenStatusOk() throws Exception {
        //given
        userDto = UserDto.builder().name(name).email(email).build();
        Mockito.when(userService.addUser(userDto)).thenAnswer(
                invocationOnMock -> {
                    UserDto u = invocationOnMock.getArgument(0, UserDto.class);
                    u.setId(userId);
                    return  u;
                }
        );
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.email", is(email)));
    }

    @Test
    void postUser_whenNoName_thenStatusBadRequest() throws Exception {
        //given
        userDto = UserDto.builder().email(email).build();
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void postUser_whenBlankName_thenStatusBadRequest() throws Exception {
        //given
        userDto = UserDto.builder().name(name).build();
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUser_whenNoEmail_thenStatusBadRequest() throws Exception {
        //given
        userDto = UserDto.builder().name(name).build();
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "email", "email@host"})
    void postUser_whenBadEmail_thenStatusBadRequest(String str) throws Exception {
        //given
        userDto = UserDto.builder().email(str).build();
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchUser_whenOnlyName_thenStatusOk() throws Exception {
        //given
        String updatedName = "updated";
        userDto = UserDto.builder().id(userId).name(updatedName).email(email).build();
        Mockito.when(userService.patch(userId, userDto)).thenReturn(userDto);
        //when
        mvc.perform(patch(PATH + "/{userId}", userId)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(updatedName)))
                .andExpect(jsonPath("$.email", is(email)));
    }

    @Test
    void patchUser_whenOnlyEmail_thenStatusOk() throws Exception {
        //given
        String updatedMail = "updated@email";
        userDto = UserDto.builder().id(userId).email(updatedMail).name(name).build();
        Mockito.when(userService.patch(userId, userDto)).thenReturn(userDto);
        //when
        mvc.perform(patch(PATH + "/{userId}", userId)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.email", is(updatedMail)));
    }

    @Test
    void patchUser_whenNoPath_thenStatusInternalServerError() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(email).email(email).build();
        //when
        mvc.perform(patch(PATH)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isInternalServerError());
    }

    @Test
    void patchUser_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(name).email(email).build();
        Mockito.when(userService.patch(anyLong(), any())).thenThrow(new NotFoundException("not found"));
        //when
        mvc.perform(patch(PATH + "/{userId}", -1L)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getUser_whenInputOk_thenStatusOk() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(name).email(email).build();
        Mockito.when(userService.getById(userId)).thenReturn(userDto);
        //when
        mvc.perform(get(PATH + "/{userId}", userId)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.email", is(email)));
    }

    @Test
    void getAll_whenInputOk_thenStatusOk() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(email).email(email).build();
        Mockito.when(userService.getAll()).thenReturn(List.of(userDto));
        //when
        mvc.perform(get(PATH + "/{userId}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_whenInputOk_thenStatusOk() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(email).email(email).build();
        Mockito.when(userService.deleteById(userId)).thenReturn(Constants.SUCCESS_DELETE_MESSAGE);
        //when
        mvc.perform(delete(PATH + "/{userId}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }
}