package com.example.booking_film.service;

import com.nimbusds.jose.JOSEException;
import org.springframework.security.core.Authentication;

import java.text.ParseException;


public interface JwtService {
    String generateToken(Authentication authenticated);

    String generateRefreshToken(Authentication authenticated);

    String extractSubjectFromRefreshToken(String token) throws ParseException, JOSEException;
}
