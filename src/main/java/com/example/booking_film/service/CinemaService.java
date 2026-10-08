package com.example.booking_film.service;

import com.example.booking_film.dto.response.CinemaDTO;

import java.util.List;

public interface CinemaService {
    List<CinemaDTO> getAllCinemas(String city);

    CinemaDTO getCinemaById(Long id);

    CinemaDTO createCinema(CinemaDTO request);

    CinemaDTO updateCinema(CinemaDTO request, Long id);

    void deleteCinema(Long id);
}
