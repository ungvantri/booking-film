package com.example.booking_film.dto.response;

import com.example.booking_film.enums.AgeRating;
import com.example.booking_film.enums.MovieStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MovieDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private String posterUrl;
    private String trailerUrl;
    private LocalDate releaseDate;
    private String director;
    private String cast;
    private Double rating;
    private AgeRating ageRating;
    private MovieStatus status;
    private List<String> genreNames;
}
