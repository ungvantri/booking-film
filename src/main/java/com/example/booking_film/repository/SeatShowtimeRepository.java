package com.example.booking_film.repository;

import com.example.booking_film.entity.SeatShowtime;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface SeatShowtimeRepository extends JpaRepository<SeatShowtime, Long> {
    List<SeatShowtime> findBySeatId(Long seatId);

    List<SeatShowtime> findByShowtimeId(Long showtimeId);

    Optional<SeatShowtime> findByShowtimeIdAndSeatId(Long showtimeId, Long id);

    @Query("SELECT ss FROM SeatShowtime ss WHERE ss.showtime.id = :showtimeId AND ss.seat.id IN :seatIds AND ss.status = SeatStatus.AVAILABLE")
    List<SeatShowtime> findSeatsForBooking(@Param("showtimeId") Long showtimeId, @Param("seatIds") List<Long> seatIds);
}
