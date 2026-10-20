package com.example.booking_film.service;

import com.example.booking_film.entity.Booking;

public interface EmailService {
    void sendBookingSuccessEmail(Long bookingId);
}
