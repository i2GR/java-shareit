package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestReplyDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestDtoMapper requestMapper;

    private final ItemRequestRepository requestStorage;

    private final UserRepository userStorage;

    private final ItemRepository itemStorage;

    @Transactional
    @Override
    public ItemRequestReplyDto addRequest(Long userId, ItemRequestDto dto) {
        User requester = findRequester(userId);
        ItemRequest request = requestMapper.fromDto(dto, requester, List.of(), LocalDateTime.now());
        ItemRequest created = requestStorage.save(request);
        log.info("New item-request added with new id {}", created.getId());
        return requestMapper.toDto(created, List.of());
    }

    @Override
    public List<ItemRequestReplyDto> getRequestsByUserId(Long userId) {
        findRequester(userId);
        List<ItemRequest> requests = requestStorage.findByRequesterIdOrderByCreatedAsc(userId);
        Map<Long, Item> requestIdMapsItem = getItemsMadeForRequests(requests);
        return mapItemRequestsToRequestsReplyDto(requests, requestIdMapsItem);
    }

    @Override
    public List<ItemRequestReplyDto> getAllRequestsByAnotherUsers(Long from, Integer size, Long userId) {
        List<ItemRequest> requests = requestStorage.findAllByRequesterIdNot(userId,
                PageRequest.of((int) (from / size), size, Sort.by("created").ascending()));
        Map<Long, Item> requestIdMapsItem = getItemsMadeForRequests(requests);
        return mapItemRequestsToRequestsReplyDto(requests, requestIdMapsItem);
    }

    @Override
    public ItemRequestReplyDto getRequestById(Long requestId, Long userId) {
        findRequester(userId);
        ItemRequest request = findRequest(requestId);
        Map<Long, Item> requestIdMapsItem = getItemsMadeForRequests(List.of(request));
        return requestMapper.toDto(request, new ArrayList<>(requestIdMapsItem.values()));
    }

    /**
     * внутренний метод поиска пользователя по идентификатору
     * @param userId идентификатор пользователя
     * @return объект сущность User - пользователь, размещающий запрос на вещь
     */
    private User findRequester(Long userId) {
        return userStorage.findById(userId).orElseThrow(
                () -> {
                    log.info("User with id {} not found", userId);
                    throw new NotFoundException(format("user with id %d not found", userId));
                }
        );
    }

    /**
     * вспомогательный метод запроса на вещь из БД
     * @param requestId идентификатор запроса на вещь
     * @return сущность вещи, если она найдена в базе
     */
    private ItemRequest findRequest(Long requestId) {
        return requestStorage.findById(requestId).orElseThrow(
                () -> {
                    log.info("item-request with id {} not found", requestId);
                    throw new NotFoundException(format("item-request with id %d not found", requestId));
                }
        );
    }

    /**
     * вспомогательный метод получения вещей, созданных в ответ на размещенный запрос
     * @param requests список запросов на вещь
     * @return HashMap[идентификатор запроса-экз. вещи, созданный в ответ на запрос]
     */
    Map<Long, Item> getItemsMadeForRequests(List<ItemRequest> requests) {
        return itemStorage.findAllByRequest_IdIn(
                        requests.stream().map(ItemRequest::getId).collect(Collectors.toList()))
                        .stream()
                        .collect(toMap(item -> item.getRequest().getId(), item -> item));
    }

    /**
     * вспомогательный метод преобразования списка запросов на вещи в список dto запросов на вещи
     * @param requests список запросов на вещь
     * @param items HashMap[идентификатор запроса-экз. вещи, созданный в ответ на запрос]
     * @return список dto запросов на вещь
     */
    List<ItemRequestReplyDto> mapItemRequestsToRequestsReplyDto(List<ItemRequest> requests,
                                                                 Map<Long, Item> items) {
        return requests.stream()
                .map(r -> requestMapper.toDto(r, items.containsKey(r.getId()) ? List.of(items.get(r.getId())) : List.of()))
                .collect(Collectors.toList());
    }
}