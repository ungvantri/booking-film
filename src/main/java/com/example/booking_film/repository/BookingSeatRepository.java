package com.example.booking_film.repository;

import com.example.booking_film.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    boolean existsBySeatId(Long seatId);
}
