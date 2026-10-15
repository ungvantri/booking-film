package com.example.booking_film.repository;

import com.example.booking_film.entity.Movie;
import com.example.booking_film.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Query("SELECT m FROM Movie m WHERE (:search IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND (m.status = :status OR :status IS NULL)")
    Page<Movie> findByFilters(@Param("search") String search, @Param("status") MovieStatus status, Pageable pageable);

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);
}
