package com.example.booking_film.service;

import com.example.booking_film.dto.request.SeatEventDTO;

public interface SeatRealtimeService {
    void broadcastSeatUpdate(SeatEventDTO event);
}
