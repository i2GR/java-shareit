package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    private static final String PATH = "/items";
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private ItemResponseDto responseDto;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private long itemId = 1L;
    private long userId = 1L;
    private User user = User.builder().id(userId).email("user@host.com").name("user").build();
    private String itemName = "item";
    private String itemDescription = "description";
    private LocalDateTime commentCreated;
    private String commentText = "comment";


    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        commentCreated = LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.MILLIS);
        // Без StdDateFormat даты сериализуются в json так, что если последние милли/нано-секунды это нули(нуль), то это отбрасывается.
        // В тестах нужно представить даты как строки, последние знаки могут быть заполнены нулями
        // Не нашел пока как это побороть.
        // Ограничил сериализацию StdDateFormat()
        // Дополнительно принудительно знак для миллисекунды устанавливается в НЕ нуль
        if (commentCreated.getNano() / 1_000_000 % 10 == 0) commentCreated = commentCreated.plusNanos(1_000_000);
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
    void HttpRequest_whenXSharerUserHeaderNotProvided_then_BadRequest() throws Exception {
        //then
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(patch(PATH)).andExpect(status().isInternalServerError());
    }

    @Test
    void HttpRequest_whenWrongXSharerUserHeader_thenBadRequest() throws Exception {
        //setup
        Mockito.when(itemService.addItem(anyLong(), any()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemService.patch(anyLong(), anyLong(), any()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemService.getByOwnerById(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemService.getAllByUserId(any(), any(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any()))
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
        Mockito.when(itemService.addItem(userId, itemDto)).thenReturn(response);
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

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void postItem_whenDtoHasNoDescription_thenStatusBadRequest(String descr) throws Exception {
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

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void postItem_whenDtoHasNoName_thenStatusBadRequest(String name) throws Exception {
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

    @Test
    void postItem_whenDtoHasNullValue_thenStatusBadRequest() throws Exception {
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
    void postItem_whenUserNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(itemService.addItem(anyLong(), any())).thenThrow(new NotFoundException("not found message"));
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
        Mockito.when(itemService.patch(1L, 1L, onlyNameDto)).thenReturn(itemDto);
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
        Mockito.when(itemService.patch(1L, 1L, onlyNameDto)).thenReturn(itemDto);
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
    void patchItem_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(itemService.patch(anyLong(), anyLong(), any())).thenThrow(new NotFoundException("not found message"));
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
        Mockito.when(itemService.getByOwnerById(anyLong(), anyLong())).thenThrow(new NotFoundException("not found message"));
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
        Mockito.when(itemService.getByOwnerById(anyLong(), anyLong())).thenReturn(responseDto);
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
        Mockito.when(itemService.getAllByUserId(anyLong(), any(), anyLong()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(responseDto.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(responseDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(true), Boolean.class));
        Mockito.verify(itemService).getAllByUserId(0L, 20, userId);
    }

    @Test
    void getAllByUserId_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(itemService.getAllByUserId(anyLong(), any(), anyLong()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, userId)
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
        Mockito.verify(itemService).getAllByUserId(1L, 2, userId);
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
        Mockito.verify(itemService, never()).getAllByUserId(any(), any(), any());
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
        Mockito.verify(itemService, never()).getAllByUserId(any(), any(), any());
    }

    @Test
    void searchItems() {
    }

    @Test
    void searchItems_whenQueryNotProvided_thenInternalServerError() throws Exception {
        //given
        Mockito.when(itemService.search(anyString(), anyLong(), anyInt()))
                .thenReturn(List.of(itemDto));
        //when
        mvc.perform(get(PATH + "/search")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
        Mockito.verify(itemService, never()).search(anyString(), anyLong(), anyInt());
    }

    @Test
    void searchItems_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(itemService.search(anyString(), anyLong(), anyInt()))
                .thenReturn(List.of(itemDto));
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
        Mockito.verify(itemService).search("query", 1L, 2);
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
        Mockito.verify(itemService, never()).search(anyString(), anyLong(), anyInt());
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
        Mockito.verify(itemService, never()).search(anyString(), anyLong(), anyInt());
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
        Mockito.verify(itemService, never()).addComment(anyLong(), anyLong(), any());
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
        Mockito.verify(itemService, never()).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void addComment_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().text(commentText).build();
        Mockito.when(itemService.addComment(anyLong(), anyLong(), any())).thenThrow(new NotFoundException("not found message"));
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
        CommentDto commentDto = CommentDto.builder().text(commentText).build();
        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("comment")
                .authorName(user.getName())
                .created(commentCreated)
                .build();
        Mockito.when(itemService.addComment(1L, 1L, commentDto)).thenReturn(commentResponseDto);
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
                .andExpect(jsonPath("$.authorName", is(user.getName()), String.class))
                .andExpect(jsonPath("$.created", is(commentCreated.format(dtf))));
        Mockito.verify(itemService, only()).addComment(userId, itemId, commentDto);
    }
}