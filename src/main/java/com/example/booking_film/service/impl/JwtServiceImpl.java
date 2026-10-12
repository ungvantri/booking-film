package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.InvalidTokenException;
import com.example.booking_film.customexception.JwtGenerationException;
import com.example.booking_film.service.JwtService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import java.util.stream.Collectors;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.key}")
    private String key;

    @Override
    public String generateToken(Authentication authenticated) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date issueTime = new Date();
        Date expiredTime = Date.from(issueTime.toInstant().plus(30, ChronoUnit.MINUTES));
        UserDetails userDetails = (UserDetails) authenticated.getPrincipal();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .claim("scope", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(" ")))
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(key));
        } catch (JOSEException e) {
            throw new JwtGenerationException("Không thể tạo JWT token", e);
        }
        return jwsObject.serialize();
    }

    @Override
    public String generateRefreshToken(Authentication authenticated) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date issueTime = new Date();
        Date expiredTime = Date.from(issueTime.toInstant().plus(30, ChronoUnit.DAYS));
        UserDetails userDetails = (UserDetails) authenticated.getPrincipal();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userDetails.getUsername())
                .issueTime(issueTime)
                .expirationTime(expiredTime)
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(key));
        } catch (JOSEException e) {
            throw new JwtGenerationException("Không thể tạo refresh token", e);
        }
        return jwsObject.serialize();
    }

    @Override
    public String extractSubjectFromRefreshToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(new MACVerifier(key))) {
                throw new InvalidTokenException("Refresh token không hợp lệ");
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expirationTime = claims.getExpirationTime();
            if (expirationTime.before(new Date())) {
                throw new InvalidTokenException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
            }
            return claims.getSubject();
        } catch (ParseException | JOSEException e) {
            throw new InvalidTokenException("Refresh token không hợp lệ");
        }
    }
}
