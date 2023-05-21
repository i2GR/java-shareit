package ru.practicum.shareit.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

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
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.validation.EndDateAfterStartDate;

import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;
import static ru.practicum.shareit.util.Constants.SHARER_USER_HTTP_HEADER;
import static ru.practicum.shareit.TestUtilities.getOkResponse;

@ContextConfiguration(classes = ShareItGateway.class)
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String PATH = "/bookings";

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private BookItemRequestDto bookingDto;

    private BookingResponseDto responseDto;

    private final long bookerId = 1L;

    private final long bookingId = 1L;

    private LocalDateTime startBooking;

    private LocalDateTime endBooking;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    @BeforeEach
    void setup() {
        startBooking = LocalDateTime.now().plusMinutes(1);
        endBooking = startBooking.plusMinutes(1);
    }

    @Test
    @DisplayName("GateWay [POST/GET/PATCH] /bookings      No X-Sharer-User-Header >> status 500")
    void httpRequest_whenXSharerUserHeaderNotProvided_then_InternalServerError() throws Exception {
        //given
        //when - then expect
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/owner")).andExpect(status().isInternalServerError());
        mvc.perform(patch(PATH + "/1?approved={true}", true)).andExpect(status().isInternalServerError());
        verify(bookingClient, never()).bookItem(anyLong(), any());
        verify(bookingClient, never()).approve(anyLong(), anyLong(), anyBoolean());
        verify(bookingClient, never()).getBookings(anyLong(), any(), anyLong(), anyInt());
        verify(bookingClient, never()).getBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
        verify(bookingClient, never()).getBooking(anyLong(), anyLong());
    }



    @Test
    @DisplayName("GateWay [POST/GET/PATCH] /bookings     X-Sharer-User-Header value not found (on Server) >> status 500")
    void httpRequest_whenWrongXSharerUserHeader_thenNotFound() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingClient.bookItem(anyLong(), any()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingClient.approve(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingClient.getBookings(anyLong(), any(), anyLong(), anyInt()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingClient.getBookingsByOwner(anyLong(), any(), anyLong(), anyInt()))
                .thenThrow(new NotFoundException("not found"));
        Mockito.when(bookingClient.getBooking(anyLong(), anyLong()))
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
        Mockito.when(bookingClient.bookItem(bookerId, bookingDto)).thenReturn(getOkResponse(responseDto));
        //when
        mvc.perform(post("/bookings")
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
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
    void postBooking_whenDtoHasStartDateInPast_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking.minusMinutes(10), endBooking);
        //when
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasEndDateInPast_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking.minusMinutes(10));
        //when
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasStartDateEqualsEndDates_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking, startBooking);
        //when
        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        //
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasEndDateEarlierStartDates_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking, startBooking.minusSeconds(30));
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoWithoutDates_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(null, null);
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(SHARER_USER_HTTP_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_whenOwnerIdIsOkNewStatusApproved_thenStatusOk() throws Exception {
        //given
        long ownerId = 2L;
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingClient.approve(anyLong(), anyLong(), anyBoolean())).thenReturn(getOkResponse(responseDto));
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
    void approveBooking_whenRequestParamApprovedNotProvided_thenStatus500() throws Exception {
        //when
        mvc.perform(patch(PATH + "/{bookingId}", bookingId)
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //when
                .andExpect(status().isInternalServerError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"any", ""})
    void approveBooking_whenRequestParamApprovedIsWrong_thenStatus500(String str) throws Exception {
        //when
        mvc.perform(patch(PATH + "/{bookingId}", bookingId)
                        .param("approved", str)
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingById_whenBookingIsPresent_thenStatusOk() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(getOkResponse(responseDto));
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
        Mockito.when(bookingClient.getBooking(anyLong(), anyLong())).thenThrow(new NotFoundException("not found message"));
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
        Mockito.when(bookingClient.getBookings(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(getOkResponse(List.of(responseDto)));
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
        Mockito.verify(bookingClient).getBookings(bookerId, BookingState.ALL, 0L, 10);
    }

    @Test
    void getBookingByBooker_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingClient.getBookings(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(getOkResponse(List.of(responseDto)));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .param("state", "current")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).getBookings(bookerId, BookingState.CURRENT, 1L, 2);
    }

    @Test
    void getBookingByBooker_whenBadParamsForStatus_thenInternalError() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        //Mockito.when(bookingClient.getBookings(anyLong(), any(), anyLong(), anyInt()))
        //        .thenReturn(getOkResponse(List.of(responseDto)));
        //when
        mvc.perform(get(PATH)
                        .header(SHARER_USER_HTTP_HEADER, bookerId)
                        .param("state", "any")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
        Mockito.verify(bookingClient, never()).getBookings(anyLong(), any(), anyLong(), anyInt());
    }

    @Test
    void getBookingsByOwner_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingClient.getBookingsByOwner(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(getOkResponse(List.of(responseDto)));
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
        Mockito.verify(bookingClient).getBookingsByOwner(2L, BookingState.ALL, 0L, 20);
    }

    @Test
    void getBookingByOwner_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingClient.getBookingsByOwner(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(getOkResponse(List.of(responseDto)));
        //when
        mvc.perform(get(PATH + "/owner")
                        .header(SHARER_USER_HTTP_HEADER, 2L) //any id for owner
                        .param("state", "current")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk());
        Mockito.verify(bookingClient).getBookingsByOwner(2L, BookingState.CURRENT, 1L, 2);
    }

    @Test
    void getBookingByOwner_whenBadParamsForStatus_thenInternalServerError() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        Mockito.when(bookingClient.getBookingsByOwner(anyLong(), any(), anyLong(), anyInt()))
                .thenReturn(getOkResponse(List.of(responseDto)));
        //when
        mvc.perform(get(PATH + "/owner")
                        .header(SHARER_USER_HTTP_HEADER, 2L) //any id for owner
                        .param("state", "any")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
        Mockito.verify(bookingClient, never()).getBookingsByOwner(anyLong(), any(), anyLong(), anyInt());
    }

    /**
     * вспомогательный метод настройки dto дял теста
     * @param start время начала бронирования
     * @param end время конца бронирования
     */
    private void setupEntityDtos(LocalDateTime start, LocalDateTime end) {
        bookingDto = BookItemRequestDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();
        responseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .status(BookingState.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("item name").build())
                .booker(BookingResponseDto.BookerDto.builder().id(bookerId).name("booker name").build())
                .build();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @EndDateAfterStartDate
    @EqualsAndHashCode(exclude = {"id"})
    private static class BookingResponseDto {

        @Setter
        private Long id;

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        private LocalDateTime start;

        @JsonFormat(pattern = DATE_TIME_PATTERN)
        private LocalDateTime end;

        private ItemDto item;

        private BookerDto booker;

        private BookingState status;

        @Getter
        @Builder
        @EqualsAndHashCode
        public static class BookerDto {
            private long id;
            private String name;
        }

        @Getter
        @Builder
        @EqualsAndHashCode
        public static class ItemDto {
            private long id;
            private String name;
        }
    }
}
