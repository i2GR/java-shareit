package ru.practicum.shareit.item;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.validation.EndDateAfterStartDate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static ru.practicum.shareit.TestUtilities.getOkResponse;
import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

@ContextConfiguration(classes = ShareItGateway.class)
@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String PATH = "/items";

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private ItemResponseDto responseDto;
    private final long itemId = 1L;
    private final long userId = 1L;
    private final String userName = "user";
    private final String itemName = "item";
    private final String itemDescription = "description";
    private LocalDateTime commentCreated;
    private final String commentText = "comment";

    @BeforeEach
    void setUp() {
        commentCreated = LocalDateTime.now();
        itemDto = ItemDto.builder()
                .name(itemName)
                .description(itemDescription)
                .available(true)
                .build();
        responseDto = ItemResponseDto.builder()
                .id(1L)
                .name(itemName)
                .description(itemDescription)
                .available(true)
                .build();
    }

    @Test
    void httpRequest_whenXSharerUserHeaderNotProvided_then_InternalServerError() throws Exception {
        //then
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(patch(PATH, "")).andExpect(status().isInternalServerError());
        mvc.perform(delete(PATH)).andExpect(status().isInternalServerError());
        verify(itemClient, never()).addItem(anyLong(), any());
        verify(itemClient, never()).updateItem(anyLong(), anyLong(), any());
        verify(itemClient, never()).getByOwnerById(anyLong(), anyLong());
        verify(itemClient, never()).getAllByUserId(anyLong(), anyInt(), anyLong());
        verify(itemClient, never()).searchItems(anyString(), anyLong(), anyInt());
        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void httpRequest_whenWrongXSharerUserHeader_thenBadRequest() throws Exception {
        //setup
        Mockito.when(itemClient.addItem(anyLong(), any()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemClient.updateItem(anyLong(), anyLong(), any()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemClient.getByOwnerById(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemClient.getAllByUserId(anyLong(), anyInt(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemClient.searchItems(anyString(), anyLong(), anyInt()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemClient.addComment(anyLong(), anyLong(), any()))
                .thenThrow(new BadRequestException("bad request message"));
        //when - then expect
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(patch(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/search")).andExpect(status().isInternalServerError());
    }

    @Test
    void postItem_whenValidDtoAndUser_thenStatusOk() throws Exception {
        //given
        ItemDto response = ItemDto.builder()
                .id(1L)
                .name(itemName)
                .description(itemDescription)
                .available(true)
                .build();
        Mockito.when(itemClient.addItem(userId, itemDto)).thenReturn(getOkResponse(response));
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName()), String.class))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.available", is(true), Boolean.class));
    }

    @ParameterizedTest(name = "value = [{arguments}]")
    @NullSource
    @ValueSource(strings = {" ", ""})
    @DisplayName("GateWay: POST /items    Bad ItemDto.description value >> status 400")
    void postItem_whenBadDescription_thenStatusBadRequest(String descr) throws Exception {
        //given
        ItemDto dtoEmptyDescription = ItemDto.builder().name(itemName).description(descr).available(true).build();
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dtoEmptyDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }


    @ParameterizedTest(name = "value = [{arguments}]")
    @NullSource
    @ValueSource(strings = {" ", ""})
    @DisplayName("GateWay: POST /items    Bad ItemDto.name value >> status 400")
    void postItem_whenBadName_thenStatusBadRequest(String name) throws Exception {
        //given
        ItemDto dtoEmptyName = ItemDto.builder().name(name).description(itemDescription).available(true).build();
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dtoEmptyName))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "value = [{arguments}]")
    @NullSource
    @ValueSource(strings = {" ", "", "any"})
    @DisplayName("GateWay: POST /items    Bad ItemDto.available value >> status 400")
    void postItem_whenBadAvailable_thenStatusBadRequest(String name) throws Exception {
        //given
        ItemDto dtoNullAvailable = ItemDto.builder().name(itemName).description(itemDescription).build();
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dtoNullAvailable))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GateWay POST /items     user (owner) not found >> status 404")
    void postItem_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(itemClient.addItem(anyLong(), any())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 2L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void patchItem_whenOnlyNameProvided_thenStatusOk() throws Exception {
        //given
        ItemDto onlyNameDto = ItemDto.builder().name("updated").build();
        Mockito.when(itemClient.updateItem(1L, 1L, onlyNameDto)).thenReturn(getOkResponse(itemDto));
        //when
        mvc.perform(patch(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .content(objectMapper.writeValueAsString(onlyNameDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void patchItem_whenOnlyDescriptionProvided_thenStatusOk() throws Exception {
        //given
        ItemDto onlyNameDto = ItemDto.builder().description("updated").build();
        Mockito.when(itemClient.updateItem(1L, 1L, onlyNameDto)).thenReturn(getOkResponse(itemDto));
        //when
        mvc.perform(patch(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .content(objectMapper.writeValueAsString(onlyNameDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GateWay PATCH /items    User (owner) not found >> status 404")
    void patchItem_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(itemClient.updateItem(anyLong(), anyLong(), any())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(patch(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, 2L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getItem_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(itemClient.getByOwnerById(anyLong(), anyLong())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getItem_whenValidData_thenStatusOk() throws Exception {
        //given
        Mockito.when(itemClient.getByOwnerById(anyLong(), anyLong())).thenReturn(getOkResponse(responseDto));
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void getAllByUserId_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        Mockito.when(itemClient.getAllByUserId(anyLong(), anyInt(), anyLong()))
                .thenReturn(getOkResponse(List.of(responseDto)));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(responseDto.getName())))
                .andExpect(jsonPath("$[0].description", is(responseDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(true), Boolean.class));
        verify(itemClient).getAllByUserId(0L, 20, userId);
    }

    @Test
    void getAllByUserId_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(itemClient.getAllByUserId(anyLong(), anyInt(), anyLong()))
                .thenReturn(getOkResponse(List.of(responseDto)));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
        verify(itemClient).getAllByUserId(1L, 2, userId);
    }

    @Test
    void getAllByUserId_whenBadRequestParamFrom_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .param("from", "-1")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        verify(itemClient, never()).getAllByUserId(anyLong(), anyInt(), anyLong());
    }

    @Test
    void getAllByUserId_whenBadRequestParamSize_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        verify(itemClient, never()).getAllByUserId(anyLong(), anyInt(), anyLong());
    }

    @Test
    void searchItems_whenQueryNotProvided_thenInternalServerError() throws Exception {
        //given
        Mockito.when(itemClient.searchItems(anyString(), anyLong(), anyInt()))
                .thenReturn(getOkResponse(List.of(itemDto)));
        //when
        mvc.perform(get(PATH + "/search")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
        verify(itemClient, never()).searchItems(anyString(), anyLong(), anyInt());
    }

    @Test
    void searchItems_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(itemClient.searchItems(anyString(), anyLong(), anyInt()))
                .thenReturn(getOkResponse(List.of(itemDto)));
        //when
        mvc.perform(get(PATH + "/search")
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .param("text", "query")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(true), Boolean.class));
        verify(itemClient).searchItems("query", 1L, 2);
    }

    @Test
    void searchItems_whenBadRequestParamFrom_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH + "/search")
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .param("from", "-1")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        verify(itemClient, never()).searchItems(anyString(), anyLong(), anyInt());
    }

    @Test
    void searchItems_whenBadRequestParamSize_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH + "/search")
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        verify(itemClient, never()).searchItems(anyString(), anyLong(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void addComment_whenDtoBlankText_thenStatusBadRequest(String text) throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().text(text).build();
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenDtoHasNullText_thenStatusBadRequest() throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().build();
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().text(commentText).build();
        Mockito.when(itemClient.addComment(anyLong(), anyLong(), any())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_whenValidDtoAndUser_thenStatusOk() throws Exception {
        //given
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        CommentDto commentDto = CommentDto.builder().text(commentText).build();
        ItemResponseDto.CommentResponseDto commentResponseDto = ItemResponseDto.CommentResponseDto.builder()
                .id(1L)
                .text("comment")
                .authorName(userName)
                .created(commentCreated)
                .build();
        Mockito.when(itemClient.addComment(1L, 1L, commentDto)).thenReturn(getOkResponse(commentResponseDto));
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentResponseDto.getText()), String.class))
                .andExpect(jsonPath("$.authorName", is(userName), String.class))
                .andExpect(jsonPath("$.created", is(commentCreated.format(dtf))));
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @EndDateAfterStartDate
    @EqualsAndHashCode(exclude = {"id"})
    private static class ItemResponseDto {

        private Long id;

        private String name;

        private String description;

        private Boolean available;

        private BookingDto lastBooking;

        private BookingDto nextBooking;

        @Setter
        private List<CommentResponseDto> comments;

        private Long requestId;

        @Builder
        @Getter
        public static class BookingDto {
            Long id;
            LocalDateTime start;
            LocalDateTime end;
            Long bookerId;
        }

        @Getter
        @Builder
        @EqualsAndHashCode
        public static class CommentResponseDto {
            private Long id;
            private String text;
            private String authorName;
            @JsonFormat(pattern = DATE_TIME_PATTERN)
            private LocalDateTime created;
        }
    }
}