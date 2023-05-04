package ru.practicum.shareit.request;

import org.mapstruct.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.CommentDtoMapper;
import ru.practicum.shareit.item.ItemDtoMapper;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestReplyDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * ItemRequest <=> Dto Mapstruct-маппер <p>
 * ТЗ-15 <p>
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface ItemRequestDtoMapper {

    @Mapping(target = "items", source = "items")
    @Mapping(target = "id", source = "itemRequest.id")
    ItemRequestReplyDto toDto(ItemRequest itemRequest, List<Item> items);

    //@Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    ItemRequest fromDto(ItemRequestDto requestDto, User requester, List<Item> items, LocalDateTime created);

    //@Mapping(target = "id", source = "item.id")
    @Mapping(target = "requestId", source = "item.request.id")
    ItemRequestReplyDto.ItemDto map(Item item);
}