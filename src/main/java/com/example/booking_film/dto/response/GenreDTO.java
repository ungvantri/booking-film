package com.example.booking_film.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenreDTO {
    private Long id;

    @NotBlank(message = "Không được để trống")
    private String name;
}
