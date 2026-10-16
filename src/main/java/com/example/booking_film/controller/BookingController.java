package com.example.booking_film.controller;

import com.example.booking_film.dto.request.BookingRequest;
import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.BookingResponse;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.enums.BookingStatus;
import com.example.booking_film.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ApiResponse<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        BookingResponse bookingResponse = bookingService.createBooking(request, email);
        return ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Tạo vé thành công")
                .data(bookingResponse)
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<BookingResponse>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        PageResponse<BookingResponse> pages = bookingService.getMyBookings(email, page, size);
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .success(true)
                .message("Lấy danh sách đặt vé của người dùng thành công")
                .data(pages)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        BookingResponse bookingResponse = bookingService.getBookingById(email, id);
        return ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Lấy thông tin đặt phòng thành công")
                .data(bookingResponse)
                .build();
    }

    @GetMapping("/code/{bookingCode}")
    public ApiResponse<BookingResponse> getBookingByCode(@PathVariable String bookingCode) {
        BookingResponse bookingResponse = bookingService.getBookingByCode(bookingCode);
        return ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Kiểm tra vé thành công")
                .data(bookingResponse)
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        bookingService.cancelBooking(email, id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Hủy vé thành công")
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<BookingResponse>> getAllBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<BookingResponse> pages = bookingService.getAllBookings(status, userId, date, page, size);
        return ApiResponse.<PageResponse<BookingResponse>>builder()
                .success(true)
                .message("Lấy danh sách đặt vé thành công")
                .data(pages)
                .build();
    }
}
