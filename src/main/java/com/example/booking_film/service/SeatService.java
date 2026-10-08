package com.example.booking_film.service;

import com.example.booking_film.dto.response.SeatDTO;

import java.util.List;

public interface SeatService {
    List<SeatDTO> getSeatsByRoom(Long roomId);

    SeatDTO updateSeat(Long id, SeatDTO request);

    void deleteSeat(Long seatId);
}
