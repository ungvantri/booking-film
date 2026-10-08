package com.example.booking_film.dto.request;

import com.example.booking_film.enums.ShowtimeStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class ShowtimeRequest {
    @NotNull
    private Long movieId;

    @NotNull
    private Long roomId;

    @NotNull
    @Future(message = "Thời gian bắt đầu phải ở tương lai")
    private LocalDateTime startTime;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    @NotNull
    private ShowtimeStatus status;
}
