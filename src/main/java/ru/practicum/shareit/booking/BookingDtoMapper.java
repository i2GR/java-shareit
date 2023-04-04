package ru.practicum.shareit.booking;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

/**
 * Booking <=> BookingDto Mapstruct-маппер <p>
 * ТЗ-13 <p>
 * expect Sprint add-bookings specs for more details
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface BookingDtoMapper {

    BookingDtoMapper INSTANCE = Mappers.getMapper(BookingDtoMapper.class);

    BookingDto toDto(Booking booking);

    Booking fromDto(BookingDto booking);

    @InheritConfiguration
    Booking update(BookingDto source, @MappingTarget Booking booking);
}