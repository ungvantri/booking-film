package com.example.booking_film.mapper;

import com.example.booking_film.dto.response.GenreDTO;
import com.example.booking_film.entity.Genre;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    List<GenreDTO> toDTO(List<Genre> genre);
    Genre toEntity(GenreDTO genreDTO);
}
