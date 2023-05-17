package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestReplyDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String PATH = "/requests";

    @MockBean
    private ItemRequestService itemRequestService;

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
    void httpRequest_whenWrongXSharerUserHeader_thenBadRequest() throws Exception {
        //setup
        Mockito.when(itemRequestService.addRequest(anyLong(), any()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemRequestService.getRequestsByUserId(anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemRequestService.getAllRequestsByAnotherUsers(anyLong(), any(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        Mockito.when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        //when - then expect
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/all")).andExpect(status().isInternalServerError());
    }

    @Test
    void addNewItemRequest_whenInputDataOk_thenStatusOk() throws Exception {
        //given
        Mockito.when(itemRequestService.addRequest(user2Id, requestDto)).thenReturn(itemRequestReplyDto);
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

    @Test
    void addNewItemRequest_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(itemRequestService.addRequest(anyLong(), any())).thenThrow(new NotFoundException("not found message"));
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
        Mockito.when(itemRequestService.getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong()))
                .thenReturn(List.of(itemRequestReplyDto));
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
        Mockito.verify(itemRequestService).getAllRequestsByAnotherUsers(0L, 20, 1L);
    }

    @Test
    void getAllRequestsByAnotherUsers_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(itemRequestService.getAllRequestsByAnotherUsers(anyLong(), anyInt(), anyLong()))
                .thenReturn(List.of(itemRequestReplyDto));
        //when
        mvc.perform(get(PATH + "/all")
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
        Mockito.verify(itemRequestService).getAllRequestsByAnotherUsers(1L, 2, 1L);
    }

    @Test
    void getRequestsByUserId_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        Mockito.when(itemRequestService.getRequestsByUserId(anyLong()))
                .thenReturn(List.of(itemRequestReplyDto));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, user2Id)
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_whenValidData_thenStatusOk() throws Exception {
        //given
        Mockito.when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(itemRequestReplyDto);
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(itemRequestService.getRequestById(anyLong(), anyLong())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(SHARER_USER_HTTP_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }
}