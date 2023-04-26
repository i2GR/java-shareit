package ru.practicum.shareit.request;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

/**
 * ItemRequest <=> ItemRequestDto Mapstruct-маппер <p>
 * ТЗ-13 <p>
 * @implNote expect Sprint 15 add-item-requests for more details
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface ItemRequestDtoMapper {

    ItemRequestDto toDto(ItemRequest itemRequest);

    ItemRequest fromDto(ItemRequestDto itemRequest);

    @InheritConfiguration
    ItemRequest update(ItemRequestDto source, @MappingTarget ItemRequest destination);
}