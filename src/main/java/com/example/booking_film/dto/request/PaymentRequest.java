package com.example.booking_film.dto.request;

import com.example.booking_film.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
public class PaymentRequest {
    @NotNull(message = "bookingId không được để trống")
    private Long bookingId;

    private PaymentMethod method;
}
