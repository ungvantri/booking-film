package com.example.booking_film.service;

import com.example.booking_film.dto.request.PaymentRequest;
import com.example.booking_film.dto.response.PaymentResponse;

import java.util.Map;

public interface PaymentService {
    PaymentResponse createPayment(String email, PaymentRequest request, String ipAddress);

    PaymentResponse getPaymentById(String email, Long id);

    void vnpayCallback(Map<String, String> params);

    void refundPayment(Long id, String email, String ipAddress);
}
