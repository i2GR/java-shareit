package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    @InjectMocks
    private final BookingServiceImpl bookingService;

    @MockBean
    private final BookingRepository bookingStorage;

    @MockBean
    private final UserRepository userStorage;

    @MockBean
    private final ItemRepository itemStorage;

    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0));
    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private User user2;
    private Item item1, item2;

    private BookingDto booking1Dto;

    private Booking booking1ByUser2;
    private BookingResponseDto response1Dto;

    @BeforeEach
    void setup() {
        setupUsersAndItems();
        setupEntityDtos(DEFAULT_START_DATE, DEFAULT_END_DATE);
    }

    @Test
    void addBooking_whenInputOk_thenOk() {
        //given
        Mockito.when(userStorage.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemStorage.findById(1L)).thenReturn(Optional.of(item1));
        Mockito.when(bookingStorage.save(any())).thenAnswer(
                invocationOnMock -> {
                    Booking b = invocationOnMock.getArgument(0, Booking.class);
                    b.setId(1L);
                    return b;
                }
        );
        //when
        BookingResponseDto responseDtoResult = bookingService.addBooking(2L, booking1Dto);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(DEFAULT_START_DATE, responseDtoResult.getStart());
        assertEquals(DEFAULT_END_DATE, responseDtoResult.getEnd());
        assertEquals("item1", responseDtoResult.getItem().getName());
        assertEquals("user2", responseDtoResult.getBooker().getName());
    }

    @Test
    void addBooking_whenBookerNotFound_thenNotFoundException() {
        //given
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(2L, booking1Dto)
        );
        //then
        assertEquals(format("user with id %d not found", 2L), nfe.getMessage());
        Mockito.verify(userStorage, only()).findById(anyLong());
        Mockito.verify(itemStorage, never()).findById(anyLong());
        Mockito.verify(bookingStorage, never()).save(any());
    }

    @Test
    void addBooking_whenItemNotFound_thenNotFoundException() {
        //given
        Mockito.when(userStorage.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemStorage.findById(1L)).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(2L, booking1Dto)
        );
        //then
        assertEquals(format("item with id %d not found", 1L), nfe.getMessage());
        Mockito.verify(userStorage, only()).findById(2L);
        Mockito.verify(itemStorage, only()).findById(1L);
        Mockito.verify(bookingStorage, never()).save(any());
    }

    @Test
    void addBooking_whenBookerIsOwner_thenNotFoundException() {
        //given
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.of(user2));
        Mockito.when(itemStorage.findById(anyLong())).thenReturn(Optional.of(item2));
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(2L, booking1Dto)
        );
        //then
        assertEquals("Booker user is item-owner user", nfe.getMessage());
        Mockito.verify(userStorage, only()).findById(anyLong());
        Mockito.verify(itemStorage, only()).findById(anyLong());
        Mockito.verify(bookingStorage, never()).save(any());
    }

    @Test
    void addBooking_whenItemNotAvailable_thenBadRequestException() {
        //given
        item1.setAvailable(false);
        Mockito.when(userStorage.findById(2L)).thenReturn(Optional.of(user2));
        Mockito.when(itemStorage.findById(1L)).thenReturn(Optional.of(item1));
        //when
        BadRequestException bre = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(2L, booking1Dto)
        );
        //then
        assertEquals("Error creating booking", bre.getMessage());
        Mockito.verify(userStorage, only()).findById(2L);
        Mockito.verify(itemStorage, only()).findById(1L);
        Mockito.verify(bookingStorage, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approve_whenInputOk_thenOk(boolean approvalState) {
        //given
        Map<Boolean, BookingStatus> statuses = Map.of(true, BookingStatus.APPROVED, false, BookingStatus.REJECTED);
        Booking booking = Booking.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(BookingStatus.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        Mockito.when(bookingStorage.findById(1L)).thenReturn(Optional.of(booking));
        //when
        BookingResponseDto responseDto = bookingService.approve(1L, 1L, approvalState);
        //then
        assertEquals(statuses.get(approvalState), responseDto.getStatus());
        Mockito.verify(bookingStorage, only()).findById(1L);
    }

    @Test
    void approve_whenBookingNotFound_thenNotFoundException() {
        //given
        Long bookingId = 1L;
        Mockito.when(bookingStorage.findById(anyLong())).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> bookingService.approve(1L, bookingId, true)
        );
        //then
        assertEquals(format("Booking with Id %d not found", bookingId), nfe.getMessage());
        Mockito.verify(bookingStorage, only()).findById(anyLong());
    }

    @Test
    void approve_whenBadOwnerId_thenNotFoundException() {
        //given
        Long bookingId = 1L;
        Long ownerId = 1L;
        item1.setOwnerId(3L);
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(BookingStatus.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        Mockito.when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> bookingService.approve(ownerId, bookingId, true)
        );
        //then
        assertEquals(format("bad request of user %d", ownerId), nfe.getMessage());
        Mockito.verify(bookingStorage, only()).findById(anyLong());
    }

    @Test
    void approve_whenBookingStatusAlreadyApproved_thenBadRequestException() {
        //given
        Long bookingId = 1L;
        Long ownerId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(BookingStatus.APPROVED)
                .item(item1)
                .booker(user2)
                .build();
        Mockito.when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        BadRequestException bre = assertThrows(BadRequestException.class,
                () -> bookingService.approve(ownerId, bookingId, true)
        );
        //then
        assertEquals(format("bad status of booking %s", BookingStatus.APPROVED), bre.getMessage());
        Mockito.verify(bookingStorage, only()).findById(anyLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L})
    void getByRelatedUserId_thenInputOk_thenOk(long relatedUserId) {
        //given
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(BookingStatus.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        Mockito.when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        BookingResponseDto responseDtoResult = bookingService.getByRelatedUserId(relatedUserId, bookingId);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(DEFAULT_START_DATE, responseDtoResult.getStart());
        assertEquals(DEFAULT_END_DATE, responseDtoResult.getEnd());
        assertEquals("item1", responseDtoResult.getItem().getName());
        assertEquals("user2", responseDtoResult.getBooker().getName());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, 3L})
    void getByRelatedUserId_thenUserIdNotRelated_thenNotFoundException(long notRelatedUserId) {
        //given
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(BookingStatus.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        Mockito.when(bookingStorage.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> bookingService.getByRelatedUserId(notRelatedUserId, bookingId)
        );
        //then
        assertEquals(format("User with id %d is not related to booking", notRelatedUserId), nfe.getMessage());
        Mockito.verify(bookingStorage, only()).findById(anyLong());
    }

    @Test
    void getListByBooker_thenStatusAll_thenInvokes_FindByBookerIdOrderByStartDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking1ByUser2));

        //when
        List<BookingResponseDto> list = bookingService.getListByBooker(2L, BookingStatus.ALL, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, only()).findByBookerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByBooker_thenStatusFuture_thenInvokes_findByBookerIdAndStartIsAfterOrderByStartDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByBooker(2L, BookingStatus.FUTURE, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, only()).findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByBooker_thenStatusPast_thenInvokes_findByBookerIdAndEndIsBeforeOrderByEndDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByBookerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByBooker(2L, BookingStatus.PAST, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, only()).findByBookerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByBooker_thenStatusCurrent_thenInvokes_findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                        anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByBooker(2L, BookingStatus.CURRENT, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, only()).findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"waiting", "rejected", "canceled"})
    void getListByBooker_thenStatuses_thenInvokes_findByBookerIdAndStatusOrderByStartDesc(String str) {
        //given
        BookingStatus status = BookingStatus.fromString(str);
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByBooker(2L, status, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, only()).findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByBooker_thenUserNotExists_thenThrowNotFound() {
        //given
        Long userId = 2L;
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(false);
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () ->
                        bookingService.getListByBooker(userId, BookingStatus.WAITING, 0L, 20)
        );
        assertEquals(format("User with id %d not found", userId), nfe.getMessage());
        Mockito.verify(bookingStorage, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByBooker_thenInputOk_thenReturnDtoList() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByBookerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByBooker(2L, BookingStatus.WAITING, 0L, 20);
        //then
        assertEquals(response1Dto.getStart(), list.get(0).getStart());
        assertEquals(response1Dto.getEnd(), list.get(0).getEnd());
        assertEquals(response1Dto.getId(), list.get(0).getId());
        assertEquals(response1Dto.getStatus(), list.get(0).getStatus());
        assertEquals(response1Dto.getItem().getName(), list.get(0).getItem().getName());
        assertEquals(response1Dto.getBooker().getName(), list.get(0).getBooker().getName());
    }

    @Test
    void getListByOwner_thenStatusAll_thenInvokes_FindByItem_OwnerIdOrderByStartDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByItem_OwnerIdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByOwner(2L, BookingStatus.ALL, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, only()).findByItem_OwnerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByOwner_thenStatusFuture_thenInvokes_FindByItem_OwnerIdAndStartIsAfterOrderByStartDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByOwner(2L, BookingStatus.FUTURE, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, only()).findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByOwner_thenStatusPast_thenInvokes_FindByItem_OwnerIdAndStartIsAfterOrderByStartDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByOwner(2L, BookingStatus.PAST, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, only()).findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByOwner_thenStatusCurrent_thenInvokes_FindByItem_OwnerIdAndStartIsAfterOrderByStartDesc() {
        //given
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                        anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByOwner(2L, BookingStatus.CURRENT, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, only()).findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"waiting", "rejected", "canceled"})
    void getListByOwner_thenStatuses_thenInvokes_findByItem_OwnerIdAndStatusOrderByStartDesc(String str) {
        //given
        BookingStatus status = BookingStatus.fromString(str);
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByItem_OwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getListByOwner(2L, status, 0L, 20);
        assertEquals(1, list.size());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdOrderByStartDesc(anyLong(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndEndIsBeforeOrderByEndDesc(anyLong(), any(), any());
        Mockito.verify(bookingStorage, never()).findByItem_OwnerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc(
                anyLong(), any(), any(), any());
        Mockito.verify(bookingStorage, only()).findByItem_OwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getListByOwner_thenInputOk_thenReturnDtoList() {
        //given
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(BookingStatus.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        Mockito.when(userStorage.existsById(anyLong())).thenReturn(true);
        Mockito.when(bookingStorage.findByItem_OwnerIdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));
        //when
        List<BookingResponseDto> list = bookingService.getListByOwner(2L, BookingStatus.WAITING, 0L, 20);
        //then
        assertEquals(response1Dto.getStart(), list.get(0).getStart());
        assertEquals(response1Dto.getEnd(), list.get(0).getEnd());
        assertEquals(response1Dto.getId(), list.get(0).getId());
        assertEquals(response1Dto.getStatus(), list.get(0).getStatus());
        assertEquals(response1Dto.getItem().getName(), list.get(0).getItem().getName());
        assertEquals(response1Dto.getBooker().getName(), list.get(0).getBooker().getName());
    }

    /**
     * вспомогательный метод настройки сущностей для теста
     */
    private void setupUsersAndItems() {
        user2 = User.builder().id(2L).name("user2").email("user2@host.dom").build();
        item1 = Item.builder().id(1L).ownerId(1L).name("item1").description("description1").available(true).build();
        item2 = Item.builder().id(2L).ownerId(2L).name("item2").description("description2").available(true).build();
    }

    /**
     * вспомогательный метод настройки dto для теста
     * @param start время начала бронирования
     * @param end время конца бронирования
     */
    private void setupEntityDtos(LocalDateTime start, LocalDateTime end) {
        booking1Dto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();
        response1Dto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("item1").build())
                .booker(BookingResponseDto.BookerDto.builder().id(2L).name("user2").build())
                .build();
        booking1ByUser2 = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(item1)
                .booker(user2)
                .build();
    }
}