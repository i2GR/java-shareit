package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import static ru.practicum.shareit.util.Constants.SUCCESS_DELETE_MESSAGE;
import static ru.practicum.shareit.TestUtilities.getOkResponse;

@ContextConfiguration(classes = ShareItGateway.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    private static final String PATH = "/users";

    @MockBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private UserDto userDto;

    private final Long userId = 1L;
    private final String name = "name";
    private final String email = "email@host.dom";

    @Test
    @DisplayName("GateWay POST /users             UserDto validation is OK >> status 200")
    void postUser_whenInputDataOk_thenStatusOk() throws Exception {
        //given
        userDto = UserDto.builder().name(name).email(email).build();
        Mockito.when(userClient.addUser(userDto)).thenAnswer(
                invocationOnMock -> {
                    UserDto u = invocationOnMock.getArgument(0, UserDto.class);
                    u.setId(userId);
                    return getOkResponse(u);
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
    @DisplayName("GateWay POST /users             UserDto w/o name >> status 400")
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

    @ParameterizedTest(name = "value = [{arguments}]")
    @NullSource
    @ValueSource(strings = {"", " "})
    @DisplayName("GateWay POST /users             Bad UserDto.name value >> status 400")
    void postUser_whenBadName_thenStatusBadRequest(String str) throws Exception {
        //given
        userDto = UserDto.builder().name(str).email(email).build();
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "value = [{arguments}]")
    @NullSource
    @ValueSource(strings = {"", " ", "email", "email@host"})
    @DisplayName("GateWay POST /users            Bad UserDto.email value >> status 400")
    void postUser_whenBadEmail_thenStatusBadRequest(String str) throws Exception {
        //given
        userDto = UserDto.builder().name(name).email(str).build();
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
    @DisplayName("GateWay PATCH /users/{id}       UserDto w/ name ONLY >> status OK")
    void patchUser_whenOnlyName_thenStatusOk() throws Exception {
        //given
        String updatedName = "updated";
        UserDto dto = UserDto.builder().id(userId).name(updatedName).build();
        userDto = UserDto.builder().id(userId).name(updatedName).email(email).build();
        Mockito.when(userClient.patchUser(userId, dto)).thenReturn(getOkResponse(userDto));
        //when
        mvc.perform(patch(PATH + "/{userId}", userId)
                        .content(objectMapper.writeValueAsString(dto))
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
    @DisplayName("GateWay PATCH /users/{id}       UserDto w/ email ONLY >> status OK")
    void patchUser_whenOnlyEmail_thenStatusOk() throws Exception {
        //given
        String updatedMail = "updated@email.dom";
        UserDto dto = UserDto.builder().id(userId).email(updatedMail).build();
        userDto = UserDto.builder().id(userId).email(updatedMail).name(name).build();
        Mockito.when(userClient.patchUser(userId, dto)).thenReturn(getOkResponse(userDto));
        //when
        mvc.perform(patch(PATH + "/{userId}", userId)
                        .content(objectMapper.writeValueAsString(dto))
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
    @DisplayName("GateWay PATCH /users/{id}       PATH w/o id >> status 500")
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
    @DisplayName("GateWay PATCH /users/{id}       ID in path not found (on Server) >> status 404")
    void patchUser_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(name).email(email).build();
        Mockito.when(userClient.patchUser(anyLong(), any())).thenThrow(new NotFoundException("not found"));
        //when
        mvc.perform(patch(PATH + "/{userId}", userId)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GateWay GET /users/{id}        ID found (on Server) >> status OK")
    void getUser_whenInputOk_thenStatusOk() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(name).email(email).build();
        Mockito.when(userClient.getUserById(anyLong())).thenReturn(getOkResponse(userDto));
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
    @DisplayName("GateWay GET /users/{id}         ID not found (on Server) >> status 404")
    void getUser_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(userClient.getUserById(anyLong())).thenThrow(new NotFoundException("not found"));
        //when
        mvc.perform(get(PATH + "/{userId}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GateWay GET /users/{id}         @Positive constraint of ID fail >> status 500")
    void getUser_whenNotPositiveId_thenStatus500() throws Exception {
        //given
        //when
        mvc.perform(get(PATH + "/{userId}", 0L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GateWay GET /users              [] >> status OK")
    void getAll_whenInputOk_thenStatusOk() throws Exception {
        //given
        userDto = UserDto.builder().id(userId).name(name).email(email).build();
        Mockito.when(userClient.getAllUsers()).thenReturn(getOkResponse(List.of(userDto)));
        //when
        mvc.perform(get(PATH + "/{userId}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GateWayDELETE /users/{id}       valid ID >> status OK")
    void deleteUser_whenInputOk_thenStatusOk() throws Exception {
        //given
        Mockito.when(userClient.deleteUserById(anyLong())).thenReturn(getOkResponse(SUCCESS_DELETE_MESSAGE));
        //when
        mvc.perform(delete(PATH + "/{userId}", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GateWay DELETE /users/{id}      constraint of ID fail >> status 500")
    void deleteUser_whenNotPositiveId_thenStatus500() throws Exception {
        //given
        //when
        mvc.perform(delete(PATH + "/{userId}", 0L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isInternalServerError());
    }
}