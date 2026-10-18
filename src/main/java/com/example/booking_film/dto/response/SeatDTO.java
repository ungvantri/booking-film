package com.example.booking_film.dto.response;

import com.example.booking_film.enums.SeatStatus;
import com.example.booking_film.enums.SeatType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SeatDTO {
    private Long seatId;
    private String seatCode;
    private Integer rowIndex;
    private Integer columnIndex;

    @NotNull(message = "Loại ghế không được để trống")
    private SeatType type;
    private Long roomId;
}
