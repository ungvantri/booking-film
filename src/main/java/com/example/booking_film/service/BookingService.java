package com.example.booking_film.service;

import com.example.booking_film.dto.request.BookingRequest;
import com.example.booking_film.dto.response.BookingResponse;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.enums.BookingStatus;

import java.time.LocalDate;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request, String email);

    PageResponse<BookingResponse> getMyBookings(String email, int page, int size);

    BookingResponse getBookingById(String email, Long id);

    BookingResponse getBookingByCode(String bookingCode);

    void cancelBooking(String email, Long id);

    PageResponse<BookingResponse> getAllBookings(BookingStatus status, Long userId, LocalDate date, int page, int size);
}
