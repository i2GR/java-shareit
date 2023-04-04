package ru.practicum.shareit.request;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

/**
 * ItemRequest <=> ItemRequestDto Mapstruct-маппер <p>
 * ТЗ-13 <p>
 * @implNote expect Sprint add-item-requests for more details
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface ItemRequestDtoMapper {

    ItemRequestDtoMapper INSTANCE = Mappers.getMapper(ItemRequestDtoMapper.class);

    ItemRequestDto toDto(ItemRequest itemRequest);

    ItemRequest fromDto(ItemRequestDto itemRequest);

    @InheritConfiguration
    ItemRequest update(ItemRequestDto source, @MappingTarget ItemRequest destination);
}