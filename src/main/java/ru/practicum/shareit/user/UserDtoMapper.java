package ru.practicum.shareit.user;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

/**
 *  User <=> UserDto Mapstruct-маппер <p>
 * ТЗ-13
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface UserDtoMapper {

    UserDtoMapper INSTANCE = Mappers.getMapper(UserDtoMapper.class);

    UserDto toDto(User user);

    User fromDto(UserDto userDto);

    @InheritConfiguration
    User update(UserDto source, @MappingTarget User destination);
}