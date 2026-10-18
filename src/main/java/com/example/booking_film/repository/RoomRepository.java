package com.example.booking_film.repository;

import com.example.booking_film.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository  extends JpaRepository<Room, Long> {
    List<Room> findByCinemaId(Long cinemaId);

    boolean existsByNameAndCinemaId(String trimmedName, Long cinemaId);
}
