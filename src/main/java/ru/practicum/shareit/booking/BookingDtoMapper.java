package ru.practicum.shareit.booking;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
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

    BookingDto toDto(Booking booking);

    Booking fromDto(BookingDto booking);

    @InheritConfiguration
    Booking update(BookingDto source, @MappingTarget Booking booking);
}