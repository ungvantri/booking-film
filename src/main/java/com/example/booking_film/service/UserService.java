package com.example.booking_film.service;

import com.example.booking_film.dto.request.ChangePasswordRequest;
import com.example.booking_film.dto.request.UpdateProfileRequest;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.enums.Role;

public interface UserService {
    UserDTO getMyProfile(String email);

    UserDTO updateMyProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    PageResponse<UserDTO> getAllUsers(String search, Role role, int page, int size);

    UserDTO getUserById(Long id);

    void updateUserStatus(Long id, Boolean isActive);

    void updateUserRole(Long id, Role role);
}
