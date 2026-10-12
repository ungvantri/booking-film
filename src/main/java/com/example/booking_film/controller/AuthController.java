package com.example.booking_film.controller;

import com.example.booking_film.dto.request.LoginRequest;
import com.example.booking_film.dto.request.RefreshTokenRequest;
import com.example.booking_film.dto.request.RegisterRequest;
import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.LoginResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO userDTO = authService.register(request);
        return ApiResponse.<UserDTO>builder()
                .success(true)
                .message("Đăng ký tài khoản thành công")
                .data(userDTO)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Đăng nhập tài khoản thành công")
                .data(loginResponse)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) throws ParseException, JOSEException {
        LoginResponse loginResponse = authService.refreshToken(request);
        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Cấp lại access token thành công")
                .data(loginResponse)
                .build();
    }
}
