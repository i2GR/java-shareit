package ru.practicum.shareit.user;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
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
    User update(UserDto source, @MappingTarget User destination);
}