package com.example.booking_film.dto.response;

import com.example.booking_film.enums.RoomType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class ShowtimeDTO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long roomId;
    private String roomName;
    private RoomType roomType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
}
