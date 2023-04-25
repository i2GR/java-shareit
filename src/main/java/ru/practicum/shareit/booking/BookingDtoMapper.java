package ru.practicum.shareit.booking;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

/**
 * Booking <=> BookingDto Mapstruct-маппер <p>
 * ТЗ-13 <p>
 * expect Sprint add-bookings specs for more details
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface BookingDtoMapper {

    @Mapping(target = "id", ignore = true)
    Booking fromDto(BookingDto dto, User booker, Item item);

    BookingResponseDto toDto(Booking booking);

    @InheritConfiguration
    Booking update(BookingDto source, @MappingTarget Booking booking);
}