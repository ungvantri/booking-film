package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.ShowtimeDTO;
import com.example.booking_film.entity.Showtime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShowtimeMapper {
    @Mapping(target = "movieId", expression = "java(showtime.getMovie().getId())")
    @Mapping(target = "movieTitle", expression = "java(showtime.getMovie().getTitle())")
    @Mapping(target = "roomId", expression = "java(showtime.getRoom().getId())")
    @Mapping(target = "roomName", expression = "java(showtime.getRoom().getName())")
    @Mapping(target = "roomType", expression = "java(showtime.getRoom().getType())")
    ShowtimeDTO toDto(Showtime showtime);

    List<ShowtimeDTO> toDtoList(List<Showtime> showtime);
}
