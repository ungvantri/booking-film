package com.example.booking_film.repository;

import com.example.booking_film.entity.User;
import com.example.booking_film.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String username);

    @Query("SELECT u FROM User u WHERE (u.fullName = :search OR u.email = :search OR u.phoneNumber = :search OR :search IS NULL) AND (u.role = :role OR :role IS NULL)")
    Page<User> findByFilters(@Param("search") String search, @Param("role") Role role, Pageable pageable);
}
