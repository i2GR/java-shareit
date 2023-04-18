package ru.practicum.shareit.booking;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;

/**
 * Booking <=> BookingDto Mapstruct-маппер <p>
 * ТЗ-13 <p>
 * expect Sprint add-bookings specs for more details
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface BookingDtoMapper {

    @Mapping(target = "status", expression = "java( ru.practicum.shareit.booking.model.BookingStatus.fromString(\"waiting\") )")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    Booking fromDto(BookingDto booking);

    BookingResponseDto toDto(Booking booking);

    @InheritConfiguration
    Booking update(BookingDto source, @MappingTarget Booking booking);
}