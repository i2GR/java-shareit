package ru.practicum.shareit.item;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

/**
 * Item <=> ItemDto Mapstruct-маппер <p>
 * ТЗ-13
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface ItemDtoMapper {

    ItemDto toDto(Item item);

    Item fromDto(ItemDto dto);

    @InheritConfiguration
    Item update(ItemDto source, @MappingTarget Item destination);
}