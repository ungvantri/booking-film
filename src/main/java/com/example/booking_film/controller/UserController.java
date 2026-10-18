package com.example.booking_film.controller;

import com.example.booking_film.dto.request.ChangePasswordRequest;
import com.example.booking_film.dto.request.UpdateProfileRequest;
import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.enums.Role;
import com.example.booking_film.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDTO> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        UserDTO userDTO = userService.getMyProfile(email);
        return ApiResponse.<UserDTO>builder()
                .success(true)
                .message("Lấy thông tin người dùng thành công")
                .data(userDTO)
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<UserDTO> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        UserDTO userDTO = userService.updateMyProfile(email, request);
        return ApiResponse.<UserDTO>builder()
                .success(true)
                .message("Cập nhật thông tin người dùng thành công")
                .data(userDTO)
                .build();
    }

    @PutMapping("/me/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, @AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        userService.changePassword(email, request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Đổi mật khẩu thành công")
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<UserDTO>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<UserDTO> pageResponse = userService.getAllUsers(search, role, page, size);
        return ApiResponse.<PageResponse<UserDTO>>builder()
                .success(true)
                .message("Lấy danh sách người thành công")
                .data(pageResponse)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ApiResponse.<UserDTO>builder()
                .success(true)
                .message("Lấy thông tin của người dùng có id:" + id + " thành công")
                .data(userDTO)
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        userService.updateUserStatus(id, isActive);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Cập nhật trạng thái thành công")
                .build();
    }

    @PutMapping("/{id}/role")
    public ApiResponse<Void> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        userService.updateUserRole(id, role);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Cập nhật role thành công")
                .build();
    }
}