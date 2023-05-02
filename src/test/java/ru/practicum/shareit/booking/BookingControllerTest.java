package ru.practicum.shareit.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ErrorResponse;
import ru.practicum.shareit.exception.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;

@WebMvcTest(controllers = BookingController.class)
//@ExtendWith(MockitoExtension.class)
//@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss")
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

    private long mockedItemId = 1L;
    private long mockedBookerId = 1L;
    private long mockedBookingId = 1L;
    private String mockedItemName = "item name";
    private String mockedBookerName = "booker name";

    private LocalDateTime startBooking;

    private LocalDateTime endBooking;


    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        //mvc = MockMvcBuilders
               // .standaloneSetup(bookingController)
               // .build();
        startBooking = LocalDateTime.now().plusMinutes(1);
        endBooking = startBooking.plusMinutes(1);
    }

    @Test
    void HttpRequest_whenXSharerUserHeaderNotProvided_then_BadRequest() throws Exception {
        mvc.perform(post(PATH)).andExpect(status().isBadRequest());
        mvc.perform(get(PATH)).andExpect(status().isBadRequest());
        mvc.perform(patch(PATH)).andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenValidDtoAndBooker_thenStatusOk() throws Exception {
        bookingDto = BookingDto.builder()
                .start(startBooking)
                .end(endBooking)
                .itemId(mockedItemId)
                .build();
        responseDto = BookingResponseDto.builder()
                .id(mockedBookingId)
                .start(startBooking)
                .end(endBooking)
                .status(BookingStatus.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(mockedItemId).name(mockedItemName).build())
                .booker(BookingResponseDto.BookerDto.builder().id(mockedBookerId).name(mockedBookerName).build())
                .build();
        Mockito.when(bookingService.addBooking(mockedBookerId, bookingDto)).thenReturn(responseDto);

        mvc.perform(post("/bookings")
                .header(SHARER_USER_HTTP_HEADER, 1L)
                .content(objectMapper.writeValueAsString(bookingDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                //TODO
                //.andExpect(jsonPath("$.start", is(responseDto.getStart())))
                //.andExpect(jsonPath("$.end", is(responseDto.getEnd())))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void postBooking_whenNoBookerIdHeader_thenStatusBadRequest() throws Exception {
        bookingDto = BookingDto.builder()
                .start(startBooking)
                .end(endBooking)
                .itemId(mockedItemId)
                .build();

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasStartDateInPast_thenStatusBadRequest() throws Exception {
        bookingDto = BookingDto.builder()
                .start(startBooking.minusMinutes(10))
                .end(endBooking)
                .itemId(mockedItemId)
                .build();
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasEndDateInPast_thenStatusBadRequest() throws Exception {
        bookingDto = BookingDto.builder()
                .start(startBooking)
                .end(endBooking.minusMinutes(10))
                .itemId(mockedItemId)
                .build();
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasStartDateEqualsEndDates_thenStatusBadRequest() throws Exception {
        bookingDto = BookingDto.builder()
                .start(startBooking)
                .end(startBooking)
                .itemId(mockedItemId)
                .build();
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        //
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasStartDateEarlierEndDates_thenStatusBadRequest() throws Exception {
        bookingDto = BookingDto.builder()
                .start(startBooking)
                .end(startBooking.minusMinutes(1))
                .itemId(mockedItemId)
                .build();
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_whenOwnerIdIsOkNewStatusApproved_thenStatusOk() throws Exception{
        long ownerId = 2L;
        bookingDto = BookingDto.builder()
                .start(startBooking)
                .end(endBooking)
                .itemId(mockedItemId)
                .build();
        responseDto = BookingResponseDto.builder()
                .id(mockedBookingId)
                .start(startBooking)
                .end(endBooking)
                .status(BookingStatus.APPROVED)
                .item(BookingResponseDto.ItemDto.builder().id(mockedItemId).name(mockedItemName).build())
                .booker(BookingResponseDto.BookerDto.builder().id(mockedBookerId).name(mockedBookerName).build())
                .build();
        Mockito.when(bookingService.approve(any(), any(), any())).thenReturn(responseDto);

        mvc.perform(patch("/bookings/{bookingId}", mockedBookingId)
                    .param("approved", "true")
                    .header(SHARER_USER_HTTP_HEADER, ownerId)
                    .characterEncoding(StandardCharsets.UTF_8))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                    //TODO
                    //.andExpect(jsonPath("$.start", is(responseDto.getStart())))
                    //.andExpect(jsonPath("$.end", is(responseDto.getEnd())))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }



    @Test
    void getBookingById_whenBookingIsPresent_thenStatusOk() throws Exception{
        responseDto = BookingResponseDto.builder()
                .id(mockedBookingId)
                .start(startBooking)
                .end(endBooking)
                .status(BookingStatus.APPROVED)
                .item(BookingResponseDto.ItemDto.builder().id(mockedItemId).name(mockedItemName).build())
                .booker(BookingResponseDto.BookerDto.builder().id(mockedBookerId).name(mockedBookerName).build())
                .build();
        Mockito.when(bookingService.getByRelatedUserId(any(), any())).thenReturn(responseDto);

        mvc.perform(get("/bookings/{bookingId}", mockedBookingId)
                        .param("approved", "true")
                        .header(SHARER_USER_HTTP_HEADER, mockedBookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                //TODO
                //.andExpect(jsonPath("$.start", is(responseDto.getStart())))
                //.andExpect(jsonPath("$.end", is(responseDto.getEnd())))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(responseDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void getBookingById_whenBookerIsWrong_thenStatusNotFound() throws Exception{
        Mockito.when(bookingService.getByRelatedUserId(any(), any())).thenThrow(new NotFoundException("not found message"));

        mvc.perform(get("/bookings/{bookingId}", mockedBookingId)
                        .header(SHARER_USER_HTTP_HEADER, mockedBookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByBooker() {
    }

    @Test
    void getBookingByOwner() {
    }
}