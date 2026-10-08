package com.example.booking_film.service;

import com.example.booking_film.dto.request.MovieRequest;
import com.example.booking_film.dto.response.CinemaShowtimeGroupDTO;
import com.example.booking_film.dto.response.MovieDTO;
import com.example.booking_film.dto.response.MovieDetailDTO;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.enums.MovieStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface MovieService {
    PageResponse<MovieDTO> getMovies(String search, MovieStatus status, int page, int size, String sortBy, String direction);

    PageResponse<MovieDTO> getNowShowingMovies(int page, int size);

    PageResponse<MovieDTO> getComingSoonMovies(int page, int size);

    MovieDetailDTO getMovieById(Long id);

    List<CinemaShowtimeGroupDTO> getShowtimesByMovie(Long id, Long cinemaId, LocalDate date, String city);

    MovieDTO createMovie(MovieRequest request);

    MovieDTO updateMovie(Long id, MovieRequest request);

    void deleteMovie(Long id);

    String uploadPoster(Long id, MultipartFile file);
}
