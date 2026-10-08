package com.example.booking_film.repository;

import com.example.booking_film.entity.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    @Query("""
            SELECT s FROM Showtime s
            WHERE (:movieId IS NULL OR s.movie.id = :movieId)
            AND (:cinemaId IS NULL OR s.room.cinema.id = :cinemaId)
            AND (:startOfDay IS NULL OR s.startTime >= :startOfDay)
            AND (:endOfDay IS NULL OR s.startTime < :endOfDay)
            """)
    Page<Showtime> filter(@Param("movieId") Long movieId, @Param("cinemaId") Long cinemaId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, Pageable pageable);

    List<Showtime> findByRoomId(Long roomId);
}
