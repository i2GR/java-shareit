package ru.practicum.shareit.user;

import org.mapstruct.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

/**
 *  User <=> UserDto Mapstruct-маппер <p>
 * ТЗ-13
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface UserDtoMapper {

    UserDto toDto(User user);

    User fromDto(UserDto userDto);

    @InheritConfiguration
    @Mapping(target = "user.email", expression = "java(notNullBlankSource(dto.getEmail(), user.getEmail()))")
    @Mapping(target = "user.name", expression = "java(notNullBlankSource(dto.getName(), user.getName()))")
    void update(UserDto dto, @MappingTarget User user);

    default String notNullBlankSource(String source, String target) {
        return source != null && !source.isBlank() ? source : target;
    }
}