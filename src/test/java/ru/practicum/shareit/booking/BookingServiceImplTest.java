package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
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
class BookingServiceImplTest {

    private final BookingServiceImpl bookingService;
    private final JdbcTemplate jdbcTemplate;
    private static final Long DEFAULT_ITEM_ID = 1L;
    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0));
    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private User owner = User.builder().name("owner").email("owner@host.dom").build();
    private User booker = User.builder().name("booker").email("booker@host.dom").build();
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
                .build();

        //when
        SqlRowSet set = jdbcTemplate.queryForRowSet("SELECT id, name from users");
        int count = 0;
        long bookerId = 0;
        long ownerId = 0;
        while (set.next()) {
            if (set.getString("name").equals("booker")) bookerId = set.getLong("id");
            if (set.getString("name").equals("owner")) ownerId = set.getLong("id");
            count++;
        }
        if (count != 2 && bookerId == 0 && ownerId == 0) fail();
        BookingResponseDto booking = bookingService.addBooking(bookerId, initial);
        long bookingId = booking.getId();
        BookingResponseDto actualByOwner = bookingService.getByRelatedUserId(ownerId, bookingId);
        BookingResponseDto actualByBooker = bookingService.getByRelatedUserId(bookerId, bookingId);

        //then
        assertThat(actualByOwner)
                .isNotNull()
                .hasNoNullFieldsOrProperties();
        assertEquals(expected.getStart(), actualByOwner.getStart());
        assertEquals(expected.getEnd(), actualByOwner.getEnd());
        assertEquals("item", actualByOwner.getItem().getName());
        assertEquals("booker", actualByOwner.getBooker().getName());
        assertEquals(actualByOwner, actualByBooker);
    }
}