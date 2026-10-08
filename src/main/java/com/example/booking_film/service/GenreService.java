package com.example.booking_film.service;

import com.example.booking_film.dto.response.GenreDTO;

import java.util.List;

public interface GenreService {
    List<GenreDTO> getAllGenres();

    void createGenre(GenreDTO request);

    void updateGenre(Long id, GenreDTO request);

    void deleteGenre(Long id);
}
