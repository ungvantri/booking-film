package com.example.booking_film.service;

import com.example.booking_film.dto.request.LoginRequest;
import com.example.booking_film.dto.request.RefreshTokenRequest;
import com.example.booking_film.dto.request.RegisterRequest;
import com.example.booking_film.dto.response.LoginResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthService {
    UserDTO register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException;
}
