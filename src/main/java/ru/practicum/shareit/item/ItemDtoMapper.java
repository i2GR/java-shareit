package ru.practicum.shareit.item;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

/**
 * Item <=> ItemDto Mapstruct-маппер <p>
 * ТЗ-13
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
        , componentModel = "spring")
public interface ItemDtoMapper {
    ItemDtoMapper INSTANCE = Mappers.getMapper(ItemDtoMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "available", source = "available")
    ItemDto toDto(Item item);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "available", source = "available")
    Item fromDto(ItemDto item);

    @InheritConfiguration
    Item update(ItemDto source, @MappingTarget Item destination);
}