package com.example.booking_film.mapper;

import com.example.booking_film.dto.request.MovieRequest;
import com.example.booking_film.dto.response.MovieDTO;
import com.example.booking_film.entity.Movie;
import com.example.booking_film.repository.MovieRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    @Mapping(target = "genreNames", expression = "java(movie.getGenres().stream().map(genre -> genre.getName()).toList())")
    MovieDTO toDto(Movie movie);
    List<MovieDTO> toDtoList(List<Movie> movies);
    Movie toEntity(MovieRequest request);
}
