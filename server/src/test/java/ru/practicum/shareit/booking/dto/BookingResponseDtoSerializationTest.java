package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.util.Constants.DATE_TIME_PATTERN;

@JsonTest
class BookingResponseDtoSerializationTest {

    @Autowired
    private JacksonTester<BookingResponseDto> jacksonTester;

    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.now();

    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    @Test
    void serializeJsonTest() throws IOException {
        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("item1").build())
                .booker(BookingResponseDto.BookerDto.builder().id(1L).name("user1").build())
                .status(BookingStatus.WAITING)
                .build();

        JsonContent<BookingResponseDto> jsonContent = jacksonTester.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start").isEqualTo(dto.getStart().format(dtf));
        assertThat(jsonContent).extractingJsonPathStringValue("$.end").isEqualTo(dto.getEnd().format(dtf));
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.name").isEqualTo(dto.getItem().getName());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.booker.name").isEqualTo(dto.getBooker().getName());
        assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo(dto.getStatus().toString());
    }
}