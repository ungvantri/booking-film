package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.request.ChangePasswordRequest;
import com.example.booking_film.dto.request.UpdateProfileRequest;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.entity.User;
import com.example.booking_film.enums.Role;
import com.example.booking_film.mapper.UserMapper;
import com.example.booking_film.repository.UserRepository;
import com.example.booking_film.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO getMyProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return userMapper.toDTO(user);
    }

    @Override
    public UserDTO updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setDateOfBirth(request.getDateOfBirth());
        User saved = userRepository.save(user);
        return userMapper.toDTO(saved);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public PageResponse<UserDTO> getAllUsers(String search, Role role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> pages = userRepository.findByFilters(search, role, pageable);
        List<UserDTO> userDTOS = userMapper.toDTO(pages.getContent());
        return PageResponse.<UserDTO> builder()
                .items(userDTOS)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalItems(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .hasNext(pages.hasNext())
                .hasPrevious(pages.hasPrevious())
                .build();
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng có id: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    public void updateUserStatus(Long id, Boolean isActive) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng có id: " + id));
        user.setIsActive(isActive);
        userRepository.save(user);
    }

    @Override
    public void updateUserRole(Long id, Role role) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng có id: " + id));
        user.setRole(role);
        userRepository.save(user);
    }
}
