package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.CinemaDTO;
import com.example.booking_film.entity.Cinema;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CinemaMapper {
    List<CinemaDTO> toListDTO(List<Cinema> cinemas);
    CinemaDTO toDTO(Cinema cinema);
    Cinema toEntity(CinemaDTO cinema);
}
