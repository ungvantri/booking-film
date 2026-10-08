package com.example.booking_film.dto.response;

import com.example.booking_film.enums.BookingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String bookingCode;
    private MovieDTO movie;
    private ShowtimeDTO showtime;
    private List<SeatDTO> seats;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime bookingTime;
}
