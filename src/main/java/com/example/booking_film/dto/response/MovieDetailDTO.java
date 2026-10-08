package com.example.booking_film.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDetailDTO {
    private MovieDTO movie;
    private List<CinemaShowtimeGroupDTO> cinemaShowtimes;
}
