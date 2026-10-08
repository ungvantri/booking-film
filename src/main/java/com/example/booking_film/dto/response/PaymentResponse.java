package com.example.booking_film.dto.response;

import com.example.booking_film.enums.PaymentStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PaymentResponse {
    private String transactionCode;
    private PaymentStatus status;
    private String redirectUrl;
}
