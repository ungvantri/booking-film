package com.example.booking_film.controller;

import com.example.booking_film.dto.request.PaymentRequest;
import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.PaymentResponse;
import com.example.booking_film.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request, HttpServletRequest req, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        String ipAddress = req.getRemoteAddr();
        PaymentResponse paymentResponse = paymentService.createPayment(email, request, ipAddress);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Tạo hóa đơn thanh toán thành công")
                .data(paymentResponse)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        PaymentResponse paymentResponse = paymentService.getPaymentById(email, id);
        return ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Lấy thông tin thanh toán thành công")
                .data(paymentResponse)
                .build();
    }

    @GetMapping("/vnpay/callback")
    public ApiResponse<Void> vnpayCallback(@RequestParam Map<String, String> params) {
        paymentService.vnpayCallback(params);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Thanh toán thành công")
                .build();
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<Void> refundPayment(@PathVariable Long id, HttpServletRequest req, @AuthenticationPrincipal Jwt jwt) {
        String ipAddress = req.getRemoteAddr();
        String email = jwt.getSubject();
        paymentService.refundPayment(id, email, ipAddress);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Hoàn trả thành công")
                .build();
    }
}