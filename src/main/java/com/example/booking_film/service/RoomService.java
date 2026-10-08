package com.example.booking_film.service;

import com.example.booking_film.dto.response.RoomDTO;

import java.util.List;

public interface RoomService {
    List<RoomDTO> getRoomsByCinema(Long cinemaId);

    RoomDTO getRoomById(Long id);

    RoomDTO createRoom(Long cinemaId, RoomDTO request);

    RoomDTO updateRoom(Long id, RoomDTO request);

    void deleteRoom(Long id);

    void generateSeats(Long id, int totalRows, int totalColumns);
}
