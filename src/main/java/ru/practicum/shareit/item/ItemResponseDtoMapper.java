package ru.practicum.shareit.item;

import org.mapstruct.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * Item <=> ItemDto Mapstruct-маппер <p>
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring",
        uses = {CommentDtoMapper.class})
public interface ItemResponseDtoMapper {

    ItemResponseDto toDto(Item item);

    ItemResponseDto toDtoWithComments(Item item, List<Comment> comments);

    @InheritConfiguration
    Item update(ItemResponseDto source, @MappingTarget Item destination);

    @Mapping(target = "bookerId", source = "booking.booker.id")
    ItemResponseDto.BookingDto map(Booking booking);
}