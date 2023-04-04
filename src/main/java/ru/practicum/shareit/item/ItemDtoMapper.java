package ru.practicum.shareit.item;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

/**
 * Item <=> ItemDto Mapstruct-маппер <p>
 * ТЗ-13
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface ItemDtoMapper {
    ItemDtoMapper INSTANCE = Mappers.getMapper(ItemDtoMapper.class);

    ItemDto toDto(Item item);

    Item fromDto(ItemDto item);

    @InheritConfiguration
    Item update(ItemDto source, @MappingTarget Item destination);
}