package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceTest {

    private final BookingService bookingService;
    private final JdbcTemplate jdbcTemplate;

    private static final Long DEFAULT_OWNER_ID = 1L;
    private static final Long DEFAULT_BOOKER_ID = 2L;
    private static final Long DEFAULT_ITEM_ID = 1L;
    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0));
    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private User owner = User.builder().id(DEFAULT_OWNER_ID).name("owner").email("owner@host.dom").build();
    private User booker = User.builder().id(DEFAULT_BOOKER_ID).name("booker").email("booker@host.dom").build();
    private Item item = Item.builder().id(1L).ownerId(1L).name("item").description("description").available(true).build();
    private BookingDto initial = BookingDto.builder()
            .itemId(DEFAULT_ITEM_ID)
            .start(DEFAULT_START_DATE)
            .end(DEFAULT_END_DATE)
            .build();

    @BeforeEach
    void reinitialiseBookings() {
        jdbcTemplate.update("DELETE FROM bookings");
    }



    @Sql(scripts = "/booking.sql")
    @Test
    void addBookingAndGetById() {
        BookingResponseDto expected = BookingResponseDto.builder()
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .item(item)
                .booker(booker)
                .build();

        //when
        long assignedId = bookingService.addBooking(DEFAULT_BOOKER_ID, initial).getId();
        BookingResponseDto actual = bookingService.getByRelatedUserId(DEFAULT_OWNER_ID, assignedId);

        //then
        assertThat(actual)
                .isNotNull()
                .hasNoNullFieldsOrProperties();
        assertEquals(expected.getStart(), actual.getStart());
        assertEquals(expected.getEnd(), actual.getEnd());
        assertEquals(expected.getItem(), actual.getItem());
        assertEquals(expected.getBooker(), actual.getBooker());
    }

    /*
    private final BookingService bookingService;
    private final JdbcTemplate jdbcTemplate;

    private final Long DEFAULT_OWNER_ID = 1L;
    private final Long DEFAULT_BOOKER_ID = 2L;
    private final Long DEFAULT_ITEM_ID = 1L;
    private final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0));
    private final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private User owner = User.builder().id(DEFAULT_OWNER_ID).name("owner").email("owner@host.dom").build();
    private User booker = User.builder().id(DEFAULT_BOOKER_ID).name("booker").email("booker@host.dom").build();
    private Item item = Item.builder().id(1L).ownerId(1L).name("item").description("description").available(true).build();
    private BookingDto initial = BookingDto.builder()
            .bookerId(DEFAULT_BOOKER_ID)
            .itemId(DEFAULT_ITEM_ID)
            .start(DEFAULT_START_DATE)
            .end(DEFAULT_END_DATE)
            .build();

    @BeforeEach
    void reinitialiseBookings() {
        jdbcTemplate.update("DELETE FROM bookings");
    }



    @Sql(scripts = "/booking.sql")
    @Test
    void addBookingAndGetById() {
        BookingDto expected = initial;

        //when
        long assignedId = bookingService.addBooking(DEFAULT_BOOKER_ID, expected).getId();
        BookingDto actual = bookingService.getByRelatedUserId(DEFAULT_OWNER_ID, assignedId);

        //then
        assertThat(actual)
                .isNotNull()
                .hasNoNullFieldsOrProperties();
        assertEquals(expected, actual);
    }*/
}