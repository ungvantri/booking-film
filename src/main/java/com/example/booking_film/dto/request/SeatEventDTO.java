package com.example.booking_film.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class SeatEventDTO {
    public enum EventType { SEAT_HELD, SEAT_RELEASED, SEAT_BOOKED }

    @NotNull(message = "Loại sự kiện không được để trống")
    private EventType type;

    @NotNull(message = "ID suất chiếu không được để trống")
    private Long showtimeId;

    @NotNull(message = "ID ghế không được để trống")
    private Long seatId;

    private Long userId;
}
