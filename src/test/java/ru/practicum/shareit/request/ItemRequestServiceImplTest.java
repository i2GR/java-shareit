package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestReplyDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {

    @InjectMocks
    private final ItemRequestService requestService;

    @MockBean
    private final ItemRequestRepository requestStorage;

    @MockBean
    private final UserRepository userStorage;

    @MockBean
    private final ItemRepository itemStorage;

    private static final Long VALUE_ID_1 = 1L;

    private User user2;

    private final Long user1Id = VALUE_ID_1;
    private final Long user2Id = 2L;
    private Item item1;
    private ItemRequestDto requestByUser2Dto;
    private final Long requestByUser2Id = VALUE_ID_1;

    private ItemRequest requestByUser2;
    private LocalDateTime currentTime;

    @BeforeEach
    void setup() {
        setupUsersAndItemsAndDto();
    }

    @Test
    void addRequest_whenInputOk_thenOk() {
        //given
        Mockito.when(userStorage.findById(user2Id)).thenReturn(Optional.of(user2));
        Mockito.when(requestStorage.save(any())).thenAnswer(
                invocationOnMock -> {
                    ItemRequest i = invocationOnMock.getArgument(0, ItemRequest.class);
                    i.setId(requestByUser2Id);
                    return i;
                }
        );
        //when
        ItemRequestReplyDto responseDtoResult = requestService.addRequest(user2Id, requestByUser2Dto);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(requestByUser2Id, responseDtoResult.getId());
        assertTrue(responseDtoResult.getItems().isEmpty());
    }

    @Test
    void addRequest_whenUserNotFound_thenNotFoundException() {
        //given
        Mockito.when(userStorage.findById(anyLong())).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () -> requestService.addRequest(user2Id, requestByUser2Dto)
        );
        //then
        assertEquals(format("user with id %d not found", user2Id), nfe.getMessage());
        Mockito.verify(userStorage, only()).findById(anyLong());
        Mockito.verify(requestStorage, never()).findById(anyLong());
    }

    @Test
    void getRequestsByUserId_thenInputOkWithItems_thenWithItems() {
        //given
        Mockito.when(userStorage.findById(user2Id)).thenReturn(Optional.of(user2));
        Mockito.when(requestStorage.findByRequesterIdOrderByCreatedAsc(user2Id)).thenReturn(List.of(requestByUser2));
        Mockito.when(itemStorage.findAllByRequest_IdIn(any())).thenReturn(List.of(item1));
        //when
        List<ItemRequestReplyDto> listByUser = requestService.getRequestsByUserId(user2Id);
        //then
        assertThat(listByUser).isNotNull();
        assertEquals(1, listByUser.size());
        assertEquals(requestByUser2Id, listByUser.get(0).getId());
        assertEquals(1, listByUser.get(0).getItems().get(0).getId());
        assertEquals("item1", listByUser.get(0).getItems().get(0).getName());
        assertEquals("itemRequest", listByUser.get(0).getDescription());
        assertEquals(currentTime, listByUser.get(0).getCreated());
    }

    @Test
    void getAllRequestsByAnotherUsers_thenInputOkWithItems_thenWithItems() {
        //given
        Mockito.when(requestStorage.findAllByRequesterIdNot(
                anyLong(),
                any())).thenReturn(List.of(requestByUser2));
        Mockito.when(itemStorage.findAllByRequest_IdIn(any())).thenReturn(List.of(item1));
        //when
        List<ItemRequestReplyDto> listByByAnotherUsers = requestService.getAllRequestsByAnotherUsers(0L, 20, user1Id);
        //then
        assertThat(listByByAnotherUsers).isNotNull();
        assertEquals(1, listByByAnotherUsers.size());
        assertEquals(requestByUser2Id, listByByAnotherUsers.get(0).getId());
        assertEquals(1, listByByAnotherUsers.get(0).getItems().get(0).getId());
        assertEquals("item1", listByByAnotherUsers.get(0).getItems().get(0).getName());
        assertEquals("itemRequest", listByByAnotherUsers.get(0).getDescription());
        assertEquals(currentTime, listByByAnotherUsers.get(0).getCreated());
    }

    @Test
    void getRequestById_whenInputOk_thenOk() {
        //given
        Mockito.when(userStorage.findById(user2Id)).thenReturn(Optional.of(user2));
        Mockito.when(requestStorage.findById(requestByUser2Id)).thenReturn(Optional.of(requestByUser2));
        Mockito.when(itemStorage.findAllByRequest_IdIn(any())).thenReturn(List.of(item1));
        //when
        ItemRequestReplyDto replyDto = requestService.getRequestById(requestByUser2Id, user2Id);
        //then
        assertThat(replyDto).isNotNull();
        assertEquals(requestByUser2Id, replyDto.getId());
        assertEquals(1, replyDto.getItems().size());
        assertEquals(1, replyDto.getItems().get(0).getId());
        assertEquals("item1", replyDto.getItems().get(0).getName());
        assertEquals("itemRequest", replyDto.getDescription());
        assertEquals(currentTime, replyDto.getCreated());
    }

    @Test
    void getRequestById_whenRequestNotFound_thenThrowsNotFound() {
        //given
        Mockito.when(userStorage.findById(user2Id)).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () ->
                        requestService.getRequestById(requestByUser2Id, user2Id)
        );
        //then
        assertEquals(format("user with id %d not found", user2Id), nfe.getMessage());
        Mockito.verify(userStorage, only()).findById(anyLong());
        Mockito.verify(requestStorage, never()).findById(anyLong());
        Mockito.verify(itemStorage, never()).findAllByRequest_IdIn(any());
    }

    @Test
    void getRequestById_whenUserNotFound_thenThrowsNotFound() {
        //given
        Mockito.when(userStorage.findById(user2Id)).thenReturn(Optional.of(user2));
        Mockito.when(requestStorage.findById(requestByUser2Id)).thenReturn(Optional.empty());
        //when
        NotFoundException nfe = assertThrows(NotFoundException.class,
                () ->
                        requestService.getRequestById(requestByUser2Id, user2Id)
        );
        //then
        assertEquals(format("item-request with id %d not found", requestByUser2Id), nfe.getMessage());
        Mockito.verify(userStorage).findById(anyLong());
        Mockito.verify(requestStorage).findById(anyLong());
        Mockito.verify(itemStorage, never()).findAllByRequest_IdIn(any());
    }

    /**
     * вспомогательный метод настройки сущностей для теста
     */
    private void setupUsersAndItemsAndDto() {
        currentTime = LocalDateTime.now();
        user2 = User.builder().id(user2Id).name("user2").email("user2@host.dom").build();
        requestByUser2Dto = ItemRequestDto.builder()
                .description("itemRequest")
                .build();
        requestByUser2  = ItemRequest.builder()
                .requester(user2)
                .description("itemRequest")
                .created(currentTime)
                .id(requestByUser2Id)
                .build();
        item1 = Item.builder()
                .id(VALUE_ID_1)
                .ownerId(user1Id)
                .name("item1")
                .description("description1")
                .available(true)
                .request(requestByUser2)
                .build();
    }
}