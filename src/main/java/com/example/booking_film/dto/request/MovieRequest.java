package com.example.booking_film.dto.request;

import com.example.booking_film.enums.AgeRating;
import com.example.booking_film.enums.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MovieRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Integer duration;

    private String posterUrl;

    private String trailerUrl;

    private LocalDate releaseDate;

    private String director;

    private String cast;

    private String language;

    private String subtitle;

    @NotNull
    private AgeRating ageRating;

    @NotNull
    private MovieStatus status;

    private List<@NotNull Long> genreIds;
}
