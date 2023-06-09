package ru.practicum.shareit.item;

import org.mapstruct.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

/**
 * Item <=> ItemDto Mapstruct-маппер <p>
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring",
        uses = {CommentDtoMapper.class})
public interface ItemResponseDtoMapper {

    @Mapping(target = "requestId", source = "item.request.id", defaultExpression = "java(null)")
    ItemResponseDto toDto(Item item);

    @Mapping(target = "bookerId", source = "booking.booker.id")
    ItemResponseDto.BookingDto map(Booking booking);
}