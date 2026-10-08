package com.example.booking_film.service;

import com.example.booking_film.dto.request.ShowtimeRequest;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.dto.response.SeatDTO;
import com.example.booking_film.dto.response.ShowtimeDTO;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {
    PageResponse<ShowtimeDTO> getAllShowtimes(Long movieId, Long cinemaId, LocalDate date, int page, int size);

    ShowtimeDTO getShowtimeById(Long id);

    List<SeatDTO> getSeatsOfShowtime(Long showtimeId);

    ShowtimeDTO createShowtime(ShowtimeRequest request);

    ShowtimeDTO updateShowtime(Long id, ShowtimeRequest request);

    void cancelShowtime(Long id);
}
