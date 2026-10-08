package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.RoomDTO;
import com.example.booking_film.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(target = "cinemaId", expression = "java(room.getCinema().getId())")
    RoomDTO toDTO(Room room);

    List<RoomDTO> toDTO(List<Room> rooms);

    Room toEntity(RoomDTO roomDTO);
}
