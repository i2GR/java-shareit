package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    @InjectMocks
    private final ItemServiceImpl itemService;

    @MockBean
    private final ItemRepository itemStorage;

    @MockBean
    private final BookingRepository bookingStorage;

    @MockBean
    private final CommentRepository commentStorage;

    @MockBean
    private final UserRepository userStorage;

    @MockBean
    private final ItemRequestRepository requestStorage;

    private User user1, user2;

    private final Long user1Id = 1L;
    private final Long user2Id = 2L;
    private final Long item1Id = 1L;
    private Item item1;

    private ItemDto item1Dto;

    private ItemResponseDto response1Dto;

    private Comment comment1;

    private CommentDto comment1Dto;

    private ItemRequest request1byUser2;
    private LocalDateTime currentTime;

    private Booking booking1Next, booking1Last;

    @BeforeEach
    void setup() {
        setupUsersAndItemsAndDto();
    }

    @Test
    void addItem_whenInputOkNoRequestId_thenOk() {
        //given
        Mockito.when(userStorage.findById(user1Id)).thenReturn(Optional.of(user1));
        Mockito.when(itemStorage.save(any())).thenAnswer(
                invocationOnMock -> {
                    Item i = invocationOnMock.getArgument(0, Item.class);
                    i.setId(item1Id);
                    return i;
                }
        );
        //when
        ItemDto responseDtoResult = itemService.addItem(item1Id, item1Dto);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(item1Id, responseDtoResult.getId());
        assertEquals("item1", responseDtoResult.getName());
        assertEquals("description1", responseDtoResult.getDescription());
        assertTrue(responseDtoResult.getAvailable());
        assertNull(responseDtoResult.getRequestId());
        Mockito.verify(requestStorage, never()).findById(anyLong());
    }

    @Test
    void addItem_whenInputOkWithRequestId_thenOk() {
        //given
        ItemDto dtoWithRequestId1 = ItemDto.builder()
                .name("item1").description("description1").available(true)
                .requestId(1L).build();
        Mockito.when(userStorage.findById(user1Id)).thenReturn(Optional.of(user1));
        Mockito.when(requestStorage.findById(1L)).thenReturn(Optional.of(request1byUser2));
        Mockito.when(itemStorage.save(any())).thenAnswer(
                invocationOnMock -> {
                    Item i = invocationOnMock.getArgument(0, Item.class);
                    i.setId(item1Id);
                    return i;
                }
        );
        //when
        ItemDto responseDtoResult = itemService.addItem(item1Id, dtoWithRequestId1);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(item1Id, responseDtoResult.getId());
        assertEquals("item1", responseDtoResult.getName());
        assertEquals("description1", responseDtoResult.getDescription());
        assertTrue(responseDtoResult.getAvailable());
        assertEquals(request1byUser2.getId(), responseDtoResult.getRequestId());
        Mockito.verify(requestStorage, only()).findById(anyLong());
    }

    @Test
    void addItem_whenUserNotFound_thenNotFoundException() {
        //given
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> itemService.addItem(user1Id, item1Dto)
        );
        //then
        assertEquals(format("User with Id %d not found", user1Id), nfe.getMessage());
        Mockito.verify(userStorage, only()).findById(anyLong());
        Mockito.verify(requestStorage, never()).findById(anyLong());
    }

    @Test
    void patch_whenInputOk_thenOk() {
        //given
        ItemDto updateDtoOnlyDescription = ItemDto.builder().description("updated1").build();
        Mockito.when(itemStorage.findById(1L)).thenReturn(Optional.of(item1));
        //when
        ItemDto responseDtoResult = itemService.patch(user1Id, item1Id, updateDtoOnlyDescription);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(item1Id, responseDtoResult.getId());
        assertEquals("item1", responseDtoResult.getName());
        assertEquals("updated1", responseDtoResult.getDescription());
        assertTrue(responseDtoResult.getAvailable());
        Mockito.verify(requestStorage, never()).findById(anyLong());
    }

    @Test
    void patch_whenBadOwnerId_thenNotFoundException() {
        //given
        ItemDto updateDtoOnlyDescription = ItemDto.builder().description("updated1").build();
        Mockito.when(itemStorage.findById(item1Id)).thenReturn(Optional.of(item1));
        //when
        ForbiddenException fe = assertThrows(ForbiddenException.class,
                () -> itemService.patch(user2Id, item1Id, updateDtoOnlyDescription)
        );
        //then
        assertEquals("requesting user not match item owner", fe.getMessage());
        Mockito.verify(itemStorage, never()).save(any());
    }

    @Test
    void getByOwnerById_thenInputOkAndNoAdditionalData_thenOkNullBookingsCommentsEmpty() {
        //given
        Mockito.when(itemStorage.findById(item1Id)).thenReturn(Optional.of(item1));
        Mockito.when(bookingStorage.findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                anyLong(),
                any(),
                any())
        ).thenReturn(Optional.empty());
        Mockito.when(bookingStorage.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any())
        ).thenReturn(Optional.empty());
        Mockito.when(commentStorage.findByItem_Id(anyLong())).thenReturn(List.of());
        //when
        ItemResponseDto responseDtoResult = itemService.getByOwnerById(user1Id, item1Id);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(item1Id, responseDtoResult.getId());
        assertEquals("item1", responseDtoResult.getName());
        assertEquals("description1", responseDtoResult.getDescription());
        assertTrue(responseDtoResult.getAvailable());
        assertNotNull(responseDtoResult.getComments());
        assertTrue(responseDtoResult.getComments().isEmpty());
        assertNull(responseDtoResult.getLastBooking());
        assertNull(responseDtoResult.getNextBooking());
        Mockito.verify(bookingStorage).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(bookingStorage).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(commentStorage).findByItem_Id(anyLong());
    }

    @Test
    void getByOwnerById_thenInputOk_thenOkWithAdditionalData() {
        //given
        Mockito.when(itemStorage.findById(item1Id)).thenReturn(Optional.of(item1));
        Mockito.when(bookingStorage.findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                anyLong(),
                any(),
                any())
        ).thenReturn(Optional.of(booking1Last));
        Mockito.when(bookingStorage.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any())
        ).thenReturn(Optional.of(booking1Next));
        Mockito.when(commentStorage.findByItem_Id(anyLong())).thenReturn(List.of(comment1));
        //when
        ItemResponseDto responseDtoResult = itemService.getByOwnerById(user1Id, item1Id);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(item1Id, responseDtoResult.getId());
        assertEquals("item1", responseDtoResult.getName());
        assertEquals("description1", responseDtoResult.getDescription());
        assertTrue(responseDtoResult.getAvailable());
        assertNotNull(responseDtoResult.getComments());
        assertEquals(1, responseDtoResult.getComments().size());
        assertEquals(comment1.getId(), responseDtoResult.getComments().get(0).getId());
        assertEquals(comment1.getAuthor().getName(), responseDtoResult.getComments().get(0).getAuthorName());
        assertEquals(booking1Last.getId(), responseDtoResult.getLastBooking().getId());
        assertEquals(booking1Next.getId(), responseDtoResult.getNextBooking().getId());
        Mockito.verify(bookingStorage).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(bookingStorage).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(commentStorage).findByItem_Id(anyLong());
    }

    @Test
    void getByOwnerById_thenNotOwnerUser_thenOkWithCommentsOnlyData() {
        //given
        Mockito.when(itemStorage.findById(item1Id)).thenReturn(Optional.of(item1));
        Mockito.when(commentStorage.findByItem_Id(anyLong())).thenReturn(List.of(comment1));
        //when
        ItemResponseDto responseDtoResult = itemService.getByOwnerById(user2Id, item1Id);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(item1Id, responseDtoResult.getId());
        assertEquals("item1", responseDtoResult.getName());
        assertEquals("description1", responseDtoResult.getDescription());
        assertTrue(responseDtoResult.getAvailable());
        assertNotNull(responseDtoResult.getComments());
        assertEquals(1, responseDtoResult.getComments().size());
        assertEquals(comment1.getId(), responseDtoResult.getComments().get(0).getId());
        assertEquals(comment1.getAuthor().getName(), responseDtoResult.getComments().get(0).getAuthorName());
        assertNull(responseDtoResult.getLastBooking());
        assertNull(responseDtoResult.getNextBooking());
        Mockito.verify(bookingStorage, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(bookingStorage, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(commentStorage).findByItem_Id(anyLong());
    }


    @Test
    void getByOwnerById_thenBadItemId_thenNotFoundException() {
        //given
        Mockito.when(itemStorage.findById(any())).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> itemService.getByOwnerById(user1Id, item1Id)
        );
        //then
        assertEquals(format("Item with Id %d not found", item1Id), nfe.getMessage());
        Mockito.verify(itemStorage, only()).findById(anyLong());
        Mockito.verify(bookingStorage, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(bookingStorage, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                anyLong(),
                any(),
                any());
        Mockito.verify(commentStorage, never()).findByItem_Id(anyLong());
    }

    @Test
    void getAllByUserId_thenInputOk_thenOkAllDataAttached() {
        //given
        Mockito.when(itemStorage.findByOwnerIdEquals(anyLong(), any())).thenReturn(List.of(item1));
        Mockito.when(bookingStorage.findByItem_OwnerIdOrderByStartDesc(
                anyLong(), any())).thenReturn(List.of(booking1Last, booking1Next));
        Mockito.when(commentStorage.findByItem_OwnerIdEquals(anyLong())).thenReturn(List.of(comment1));
        //when
        List<ItemResponseDto> result = itemService.getAllByUserId(0L, 20, user1Id);
        //then
        assertThat(result).isNotNull();
        assertEquals(1, result.size());
        assertEquals(item1Id, result.get(0).getId());
        assertEquals("item1", result.get(0).getName());
        assertNotNull(result.get(0).getComments());
        assertEquals(1, result.get(0).getComments().size());
        assertEquals("comment1", result.get(0).getComments().get(0).getText());
        assertEquals(booking1Last.getId(), result.get(0).getLastBooking().getId());
        assertEquals(booking1Next.getId(), result.get(0).getNextBooking().getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void search_whenQueryIsBlank_thenEmptyList(String str) {
        //then
        assertTrue(itemService.search(str, 0L, 20).isEmpty());
    }

    @Test
    void search_whenQuery_thenResult() {
        //given
        Mockito.when(itemStorage.findDistinctByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                anyString(), anyString(), any())).thenReturn(List.of(item1));
        //when
        List<ItemDto> result = itemService.search("query", 0L, 20);
        //then
        assertEquals(1, result.size());
        assertEquals(item1.getId(), result.get(0).getId());
        assertEquals(item1.getName(), result.get(0).getName());
    }

    @Test
    void addComment_whenInputOk_thenOk() {
        //given
        Booking booking = Booking.builder()
                .id(1L)
                .start(currentTime.minusMinutes(2))
                .end(currentTime.minusMinutes(1))
                .item(item1)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();
        Mockito.when(bookingStorage.findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(
                anyLong(), anyLong(), any(), any()))
                .thenReturn(Optional.of(booking));
        Mockito.when(commentStorage.save(any())).thenAnswer(
                invocationOnMock -> {
                    Comment c = invocationOnMock.getArgument(0, Comment.class);
                    c.setId(1L);
                    return c;
                }
        );
        //when
        CommentResponseDto responseDtoResult = itemService.addComment(user2Id, item1Id, comment1Dto);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals("comment1", responseDtoResult.getText());
        assertEquals(user2.getName(), responseDtoResult.getAuthorName());
    }

    @Test
    void addComment_whenBookingNotFound_thenNotFoundException() {
        //given
        Mockito.when(bookingStorage.findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(
                        anyLong(), anyLong(), any(), any()))
                .thenReturn(Optional.empty());
       //when
        BadRequestException bre = assertThrows(BadRequestException.class,
                () -> itemService.addComment(user2Id, item1Id, comment1Dto)
        );
        //then
        assertEquals("Booking by user of Item not exists", bre.getMessage());
        Mockito.verify(commentStorage, never()).save(any());
    }

    /**
     * вспомогательный метод настройки сущностей для теста
     */
    private void setupUsersAndItemsAndDto() {
        currentTime = LocalDateTime.now();
        user1 = User.builder().id(user1Id).name("user1").email("user1@host.dom").build();
        user2 = User.builder().id(user2Id).name("user2").email("user2@host.dom").build();
        item1 = Item.builder().id(item1Id).ownerId(user1Id).name("item1").description("description1").available(true).build();
        item1Dto = ItemDto.builder()
                .name("item1")
                .description("description1")
                .available(true)
                .build();
        response1Dto = ItemResponseDto.builder()
                .id(item1Id)
                .name("name1")
                .description("description1")
                .available(true)
                .build();
        request1byUser2 = ItemRequest.builder()
                .id(1L)
                .description("request1ByUser2")
                .requester(user2)
                .created(currentTime)
                .build();
        booking1Last = Booking.builder()
                .id(1L)
                .start(currentTime.minusMinutes(2))
                .end(currentTime.minusMinutes(1))
                .item(item1)
                .booker(user2)
                .build();
        booking1Next = Booking.builder()
                .id(2L)
                .start(currentTime.plusMinutes(1))
                .end(currentTime.plusMinutes(2))
                .item(item1)
                .booker(user2)
                .build();
        comment1 = Comment.builder()
                .id(1L)
                .text("comment1")
                .item(item1)
                .author(user2)
                .build();
        comment1Dto = CommentDto.builder()
                .text("comment1")
                .build();
    }
}