package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

@ContextConfiguration(classes = ShareItServer.class)
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String PATH = "/bookings";

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private BookingDto bookingDto;

    private BookingResponseDto responseDto;

    private final long itemId = 1L;
    private final long bookerId = 1L;
    private final long bookingId = 1L;
    private final String itemName = "item name";
    private final String bookerName = "booker name";

    private LocalDateTime startBooking;
    private LocalDateTime endBooking;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    @BeforeEach
    void setup() {
        startBooking = LocalDateTime.now().plusMinutes(1);
        endBooking = startBooking.plusMinutes(1);
    }

    @Test
    void httpRequest_whenWrongXSharerUserHeader_thenNotFound() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.addBooking(anyLong(), any()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingService.getByRelatedUserId(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingService.getListByBooker(anyLong(), any(), any(), any()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingService.getListByOwner(anyLong(), any(), any(), any()))
                .thenThrow(new NotFoundException("not found"));
        //when - then expect
        mvc.perform(post(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, 1L))
                .andExpect(status().isNotFound());
        mvc.perform(get(PATH + "/owner")
                        .header(SHARER_USER_HTTP_HEADER, 1L))
                .andExpect(status().isNotFound());
        mvc.perform(patch(PATH + "/1?approved={true}", true)
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void postBooking_whenValidDtoAndBooker_thenStatusOk() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.addBooking(bookerId, bookingDto)).thenReturn(responseDto);
        //when
        mvc.perform(post("/bookings")
                .header(SHARER_USER_HTTP_HEADER, 1L)
                .content(objectMapper.writeValueAsString(bookingDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
         //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$.end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void approveBooking_whenOwnerIdIsOkNewStatusApproved_thenStatusOk() throws Exception {
        //given
        long ownerId = 2L;
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.approve(any(), any(), any())).thenReturn(responseDto);
        //when
        mvc.perform(patch(PATH + "/{bookingId}", bookingId)
                    .param("approved", "true")
                    .header(SHARER_USER_HTTP_HEADER, ownerId)
                    .characterEncoding(StandardCharsets.UTF_8))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$.end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void approveBooking_whenPathVariableBookingIdNotProvided_thenStatus500() throws Exception {
        //when
        mvc.perform(patch(PATH)
                        .param("approved", "true")
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
        //then
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingById_whenBookingIsPresent_thenStatusOk() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.getByRelatedUserId(any(), any())).thenReturn(responseDto);
        //when
        mvc.perform(get(PATH + "/{bookingId}", bookingId)
                        .param("approved", "true")
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
        //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$.end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void getBookingById_whenServiceThrowsNotFound_thenStatusNotFound() throws Exception {
        //given
        Mockito.when(bookingService.getByRelatedUserId(any(), any())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
        //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingByBooker_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.getListByBooker(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$[0].end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$[0].item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].status", is(responseDto.getStatus().toString())));
        Mockito.verify(bookingService).getListByBooker(bookerId, BookingStatus.ALL, 0L, 20);
    }

    @Test
    void getBookingByBooker_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.getListByBooker(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .param("state", "current")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk());
        Mockito.verify(bookingService).getListByBooker(bookerId, BookingStatus.CURRENT, 1L, 2);
    }

    @Test
    void getBookingByOwner_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.getListByOwner(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH + "/owner")
                        .header(SHARER_USER_HTTP_HEADER, 2L) //any id for owner
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$[0].end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$[0].item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$[0].status", is(responseDto.getStatus().toString())));
        Mockito.verify(bookingService).getListByOwner(2L, BookingStatus.ALL, 0L, 20);
    }

    @Test
    void getBookingByOwner_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingService.getListByOwner(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH + "/owner")
                        .header(SHARER_USER_HTTP_HEADER, 2L) //any id for owner
                        .param("state", "current")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk());
        Mockito.verify(bookingService).getListByOwner(2L, BookingStatus.CURRENT, 1L, 2);
    }

    /**
     * вспомогательный метод настройки dto дял теста
     * @param start время начала бронирования
     * @param end время конца бронирования
     */
    private void setupEntityDtos(LocalDateTime start, LocalDateTime end) {
        bookingDto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        responseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(itemId).name(itemName).build())
                .booker(BookingResponseDto.BookerDto.builder().id(bookerId).name(bookerName).build())
                .build();
    }
}