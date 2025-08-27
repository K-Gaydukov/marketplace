package com.example.mapper;

import com.example.dto.UserCreateDto;
import com.example.dto.UserDto;
import com.example.dto.UserUpdateDto;
import com.example.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", source = "role.name")
    UserDto toDto(User user);

    void updateFromDto(UserUpdateDto dto, @MappingTarget User user);

    @Mapping(target = "role", source = "role.name")
    User toEntity(UserCreateDto dto);

}
