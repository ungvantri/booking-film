package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    List<UserDTO> toDTO(List<User> users);
}
