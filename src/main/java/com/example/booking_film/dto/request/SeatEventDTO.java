package com.example.booking_film.dto.request;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
public class SeatEventDTO {
    public enum EventType { SEAT_HELD, SEAT_RELEASED, SEAT_BOOKED }
    private EventType type;
    private Long showtimeId;
    private Long seatId;
    private Long userId; // Dùng để client biết ghế này do chính mình hay người khác đang giữ
}
