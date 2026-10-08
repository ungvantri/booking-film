package com.example.booking_film.dto.request;

import com.example.booking_film.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class BookingRequest {
    @NotNull(message = "showtimeId không được để trống")
    private Long showtimeId;

    @NotEmpty(message = "Phải chọn ít nhất 1 ghế")
    private List<Long> seatIds;

    private String promotionCode;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;
}
