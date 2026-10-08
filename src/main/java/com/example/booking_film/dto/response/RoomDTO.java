package com.example.booking_film.dto.response;

import com.example.booking_film.enums.RoomType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RoomDTO {
    private Long id;
    private String name;
    private RoomType type;
    private Integer totalSeats;
    private Long cinemaId;
}
