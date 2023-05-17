package ru.practicum.shareit.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.TestUtilities.getOkResponse;
import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

@WebMvcTest(controllers = RequestController.class)
class RequestControllerTest {

    private static final String PATH = "/requests";

    @MockBean
    private RequestClient requestClient;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;

    private final Long user2Id = 2L;
    private ItemRequestDto requestDto;

    private ItemRequestReplyDto itemRequestReplyDto;

    LocalDateTime created = LocalDateTime.now();

    @BeforeEach
    void setup() {
        requestDto = ItemRequestDto.builder()
                .description("itemRequest")
                .build();
        itemRequestReplyDto = ItemRequestReplyDto.builder()
                .created(created)
                .id(1L)
                .description("itemRequest")
                .build();
    }

    @Test
    @DisplayName("GateWay [POST/GET/PATCH] /requests      No X-Sharer-User-Header >> status 500")
    void httpRequest_whenXSharerUserHeaderNotProvided_then_InternalServerError() throws Exception {
        //then
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/all")).andExpect(status().isInternalServerError());
        verify(requestClient, never()).addRequest(anyLong(), any());
        verify(requestClient, never()).getRequestsByUserId(anyLong());
        verify(requestClient, never()).getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong());
        verify(requestClient, never()).getRequestById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("GateWay [POST/GET/PATCH] /requests      X-Sharer-User-Header value not found (on Server) >> status 500")
    void httpRequest_whenWrongXSharerUserHeader_thenBadRequest() throws Exception {
        //given
        Mockito.when(requestClient.addRequest(anyLong(), any()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(requestClient.getRequestsByUserId(anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(requestClient.getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(requestClient.getRequestById(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        //when - then expect
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/all")).andExpect(status().isInternalServerError());
    }

    @Test
    void addNewItemRequest_whenInputDataOk_thenStatusOk() throws Exception {
        //given
        Mockito.when(requestClient.addRequest(user2Id, requestDto)).thenReturn(getOkResponse(itemRequestReplyDto));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, user2Id)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestReplyDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestReplyDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestReplyDto.getCreated().format(dtf))));
    }

    @ParameterizedTest(name = "value = [{arguments}]")
    @NullSource
    @ValueSource(strings = {" ", ""})
    @DisplayName("GateWay POST /requests                  Bad ItemRequestDto.description value >> status 400")
    void addNewItemRequest_whenBadDescription_thenStatusBadRequest(String descr) throws Exception {
        //given
        ItemRequestDto dtoEmptyDescription = ItemRequestDto.builder().description(descr).build();
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, user2Id)
                        .content(objectMapper.writeValueAsString(dtoEmptyDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewItemRequest_whenDtoHasNullValue_thenStatusBadRequest() throws Exception {
        //given
        ItemDto dtoNullDescription  = ItemDto.builder().build();
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, user2Id)
                        .content(objectMapper.writeValueAsString(dtoNullDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewItemRequest_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(requestClient.addRequest(anyLong(), any())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 2L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRequestsByAnotherUsers_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        Mockito.when(requestClient.getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong()))
                .thenReturn(getOkResponse(List.of(itemRequestReplyDto)));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        //when
        mvc.perform(get(PATH + "/all")
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestReplyDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestReplyDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestReplyDto.getCreated().format(dtf))));
        Mockito.verify(requestClient).getAllRequestsByAnotherUsers(0L, 20, 1L);
    }

    @Test
    void getAllRequestsByAnotherUsers_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(requestClient.getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong()))
                .thenReturn(getOkResponse(List.of(itemRequestReplyDto)));
        //when
        mvc.perform(get(PATH + "/all")
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
        Mockito.verify(requestClient).getAllRequestsByAnotherUsers(1L, 2, 1L);
    }

    @Test
    void getAllRequestsByAnotherUsers_whenBadRequestParamFrom_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH + "/all")
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .param("from", "-1")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
        Mockito.verify(requestClient, never()).getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong());
    }


    @Test
    void getAllRequestsByAnotherUsers_whenBadRequestParamSize_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH + "/all")
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        Mockito.verify(requestClient, never()).getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong());
    }

    @Test
    void getRequestsByUserId_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(requestClient.getRequestsByUserId(anyLong()))
                .thenReturn(getOkResponse(List.of(itemRequestReplyDto)));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, user2Id)
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
    }

    @Test
    void getRequestsByUserId_whenBadRequestParamSize_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH + "/all")
                        .header(SHARER_USER_HTTP_HEADER, user2Id)
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        Mockito.verify(requestClient, never()).getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong());
    }

    @Test
    void getRequestById_whenValidData_thenStatusOk() throws Exception {
        //given
        Mockito.when(requestClient.getRequestById(anyLong(), anyLong())).thenReturn(getOkResponse(itemRequestReplyDto));
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, user2Id)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(requestClient.getRequestById(anyLong(), anyLong())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(exclude = {"id"})
    public static class ItemRequestReplyDto {

        @Setter
        private Long id;

        private String description;

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        private LocalDateTime created;

        private List<ItemDto> items;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @EqualsAndHashCode
        public static class ItemDto {
            private Long id;
            private String name;
            private Long ownerId;
            private String description;
            private Long requestId;
            private Boolean available;
        }
    }
}