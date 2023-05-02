package ru.practicum.shareit.item;

import org.mapstruct.*;
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
    @Mapping(target = "item.name", expression = "java(notNullBlankSource(dto.getName(), item.getName()))")
    @Mapping(target = "item.description", expression = "java(notNullBlankSource(dto.getDescription(), item.getDescription()))")
    void update(ItemDto dto, @MappingTarget Item item);

    default String notNullBlankSource(String source, String target) {
        return source != null && !source.isBlank() ? source : target;
    }
}