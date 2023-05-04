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

    @Mapping(target = "requestId", source ="item.request.id", defaultExpression = "java(null)")
    //@Mapping(target = "requestId", expression = "java(item.getRequest() == null ? null : item.getRequest().getId())")
    ItemResponseDto toDto(Item item);

    @Mapping(target = "requestId", source ="item.request.id", defaultExpression = "java(null)")
    //@Mapping(target = "requestId", expression = "java(item.getRequest() == null ? null : item.getRequest().getId())")
    ItemResponseDto toDtoWithComments(Item item, List<Comment> comments);

    @Mapping(target = "bookerId", source = "booking.booker.id")
    ItemResponseDto.BookingDto map(Booking booking);
}