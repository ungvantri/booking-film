package com.example.booking_film.service;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.request.ChangePasswordRequest;
import com.example.booking_film.dto.request.UpdateProfileRequest;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.entity.User;
import com.example.booking_film.enums.Role;
import com.example.booking_film.mapper.UserMapper;
import com.example.booking_film.repository.UserRepository;
import com.example.booking_film.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("getMyProfile thành công khi email tồn tại")
    void testGetMyProfile_Success() {
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).fullName("Nguyễn Văn A").build();
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setEmail(email);
        dto.setFullName("Nguyễn Văn A");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(dto);

        UserDTO result = userService.getMyProfile(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Nguyễn Văn A", result.getFullName());
    }

    @Test
    @DisplayName("getMyProfile ném ResourceNotFoundException khi không tìm thấy user")
    void testGetMyProfile_NotFound() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.getMyProfile(email)
        );
    }

    @Test
    @DisplayName("updateMyProfile cập nhật thông tin cá nhân thành công")
    void testUpdateMyProfile_Success() {
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).fullName("Cũ").build();

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Nguyễn Văn Mới");
        request.setPhoneNumber("0987654321");
        request.setAvatarUrl("http://avatar.png");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));

        UserDTO responseDTO = new UserDTO();
        responseDTO.setId(1L);
        responseDTO.setFullName("Nguyễn Văn Mới");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(responseDTO);

        UserDTO result = userService.updateMyProfile(email, request);

        assertNotNull(result);
        assertEquals("Nguyễn Văn Mới", result.getFullName());
        assertEquals("Nguyễn Văn Mới", user.getFullName());
        assertEquals("0987654321", user.getPhoneNumber());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("changePassword thành công khi mật khẩu cũ khớp và mật khẩu mới khác mật khẩu cũ")
    void testChangePassword_Success() {
        String email = "user@example.com";
        String oldPass = "OldPass123";
        String encodedOldPass = "encodedOldPass";
        String newPass = "NewPass123";
        String encodedNewPass = "encodedNewPass";

        User user = User.builder().id(1L).email(email).password(encodedOldPass).build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(oldPass);
        request.setNewPassword(newPass);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPass, encodedOldPass)).thenReturn(true);
        when(passwordEncoder.matches(newPass, encodedOldPass)).thenReturn(false);
        when(passwordEncoder.encode(newPass)).thenReturn(encodedNewPass);

        userService.changePassword(email, request);

        assertEquals(encodedNewPass, user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("changePassword ném BadRequestException khi mật khẩu cũ không đúng")
    void testChangePassword_WrongOldPassword() {
        String email = "user@example.com";
        User user = User.builder().id(1L).email(email).password("hashedPassword").build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("WrongOldPass");
        request.setNewPassword("NewPass123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongOldPass", "hashedPassword")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userService.changePassword(email, request)
        );
        assertEquals("Mật khẩu hiện tại không chính xác", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword ném BadRequestException khi mật khẩu mới trùng mật khẩu cũ")
    void testChangePassword_SameAsOldPassword() {
        String email = "user@example.com";
        String password = "SamePassword123";
        User user = User.builder().id(1L).email(email).password("hashedPassword").build();

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(password);
        request.setNewPassword(password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userService.changePassword(email, request)
        );
        assertEquals("Mật khẩu mới không được trùng với mật khẩu hiện tại", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUserStatus cập nhật trạng thái isActive thành công")
    void testUpdateUserStatus_Success() {
        Long userId = 1L;
        User user = User.builder().id(userId).isActive(true).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserStatus(userId, false);

        assertFalse(user.getIsActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("updateUserRole cập nhật vai trò role thành công")
    void testUpdateUserRole_Success() {
        Long userId = 1L;
        User user = User.builder().id(userId).role(Role.CUSTOMER).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserRole(userId, Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository, times(1)).save(user);
    }
}
