package com.example.booking_film.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SeatMapDTO {
    private Long showtimeId;
    private String roomName;
    private Integer totalRows;
    private Integer totalColumns;
    private List<SeatDTO> seats;
}
