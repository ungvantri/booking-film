package com.example.booking_film.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CinemaDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String phoneNumber;
}
