package com.example.booking_film.repository;

import com.example.booking_film.entity.Booking;
import com.example.booking_film.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByShowtimeId(Long id);

    @Query(value = "SELECT b FROM Booking b " +
            "JOIN FETCH b.showtime s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.room r " +
            "WHERE b.user.id = :userId",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long id1);

    Optional<Booking>findByBookingCode(String bookingCode);

    @Query("SELECT b FROM Booking b WHERE (b.status = :status OR :status IS NULL) AND (b.user.id = :userId OR :userId IS NULL) AND (:start IS NULL OR b.bookingTime >= :start) AND (:end IS NULL OR b.bookingTime < :end)")
    Page<Booking> findByFilter(@Param("status") BookingStatus status, @Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
