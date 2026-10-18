package com.example.booking_film.repository;

import com.example.booking_film.entity.SeatShowtime;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public interface SeatShowtimeRepository extends JpaRepository<SeatShowtime, Long> {
    @Query("SELECT ss FROM SeatShowtime ss JOIN FETCH ss.seat s WHERE ss.showtime.id = :showtimeId")
    List<SeatShowtime> findByShowtimeIdWithSeats(@Param("showtimeId") Long showtimeId);

    Optional<SeatShowtime> findByShowtimeIdAndSeatId(Long showtimeId, Long id);

    @Query("SELECT ss FROM SeatShowtime ss WHERE ss.showtime.id = :showtimeId AND ss.seat.id IN :seatIds AND ss.status = SeatStatus.AVAILABLE")
    List<SeatShowtime> findSeatsForBooking(@Param("showtimeId") Long showtimeId, @Param("seatIds") List<Long> seatIds);

    boolean existsBySeatId(Long seatId);

    @Modifying
    @Query("UPDATE SeatShowtime ss SET ss.price = :newPrice WHERE ss.showtime.id = :showtimeId AND ss.status = com.example.booking_film.enums.SeatStatus.AVAILABLE")
    void updateAvailableSeatPrices(@Param("showtimeId") Long showtimeId, @Param("newPrice") BigDecimal newPrice);
}
