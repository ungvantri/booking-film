package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.DuplicateResourceException;
import com.example.booking_film.customexception.InvalidTokenException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.request.LoginRequest;
import com.example.booking_film.dto.request.RefreshTokenRequest;
import com.example.booking_film.dto.request.RegisterRequest;
import com.example.booking_film.dto.response.LoginResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.entity.User;
import com.example.booking_film.enums.Role;
import com.example.booking_film.mapper.UserMapper;
import com.example.booking_film.repository.UserRepository;
import com.example.booking_film.service.AuthService;
import com.example.booking_film.service.JwtService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email đã tồn tại");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Số điện thoại đã tồn tại");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
        User saved = userRepository.save(user);
        return userMapper.toDTO(saved);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authenticated = authenticationManager.authenticate(authentication);
        String accessToken = jwtService.generateToken(authenticated);
        String refreshToken = jwtService.generateRefreshToken(authenticated);
        UserDTO userDTO = userMapper.toDTO((User) authenticated.getPrincipal());
        return new LoginResponse(accessToken, refreshToken, userDTO);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException {
        String refreshToken = request.getRefreshToken();
        String email = jwtService.extractSubjectFromRefreshToken(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        if (!user.isEnabled()) {
            throw new InvalidTokenException("Tài khoản đã bị vô hiệu hóa");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        String newAccessToken = jwtService.generateToken(authentication);
        return new LoginResponse(newAccessToken, refreshToken, userMapper.toDTO(user));
    }
}
