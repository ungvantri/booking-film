package com.example.booking_film.dto.response;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaShowtimeGroupDTO {
    private Long cinemaId;
    private String cinemaName;
    private List<ShowtimeDTO> showtimes;
}
