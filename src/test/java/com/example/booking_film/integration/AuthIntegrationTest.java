package com.example.booking_film.integration;

import com.example.booking_film.dto.request.LoginRequest;
import com.example.booking_film.dto.request.RefreshTokenRequest;
import com.example.booking_film.dto.request.RegisterRequest;
import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.LoginResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.entity.User;
import com.example.booking_film.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/auth/register tạo người dùng mới thành công và lưu mật khẩu mã hóa trong database")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyễn Văn A");
        request.setEmail("nguyenvana@example.com");
        request.setPassword("Secret123!");
        request.setPhoneNumber("0987654321");

        ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getEmail()).isEqualTo("nguyenvana@example.com");

        // Kiểm tra thực tế trong database
        User savedUser = userRepository.findByEmail("nguyenvana@example.com").orElseThrow();
        assertThat(savedUser.getFullName()).isEqualTo("Nguyễn Văn A");
        assertThat(savedUser.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(passwordEncoder.matches("Secret123!", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/register với email đã tồn tại phải trả về 409 Conflict")
    void testRegister_DuplicateEmail() {
        User existingUser = User.builder()
                .fullName("Trùng Email")
                .email("duplicate@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Người Dùng Mới");
        request.setEmail("duplicate@example.com");
        request.setPassword("NewPassword123!");
        request.setPhoneNumber("0911223344");

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    @DisplayName("POST /api/auth/register với dữ liệu không hợp lệ trả về 400 Bad Request")
    void testRegister_ValidationError() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName(""); // blank name
        request.setEmail("not-an-email"); // invalid email
        request.setPassword("123"); // too short

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).isNotEmpty();
    }

    @Test
    @DisplayName("POST /api/auth/login trả về JWT token và refresh token khi thông tin chính xác")
    void testLogin_Success() {
        User user = User.builder()
                .fullName("Đăng Nhập Test")
                .email("login_test@example.com")
                .password(passwordEncoder.encode("CorrectPass123!"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login_test@example.com");
        loginRequest.setPassword("CorrectPass123!");

        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getData().getRefreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("POST /api/auth/login sai mật khẩu trả về 401 Unauthorized")
    void testLogin_WrongPassword() {
        User user = User.builder()
                .fullName("Test Sai Pass")
                .email("wrong_pass@example.com")
                .password(passwordEncoder.encode("RightPass123!"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrong_pass@example.com");
        loginRequest.setPassword("IncorrectPass!");

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    @DisplayName("POST /api/auth/refresh cấp token mới khi refresh token hợp lệ")
    void testRefreshToken_Success() {
        User user = User.builder()
                .fullName("Refresh Test")
                .email("refresh_test@example.com")
                .password(passwordEncoder.encode("Pass123!"))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("refresh_test@example.com");
        loginRequest.setPassword("Pass123!");

        ResponseEntity<ApiResponse<LoginResponse>> loginResponse = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {}
        );

        String refreshToken = loginResponse.getBody().getData().getRefreshToken();

        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken(refreshToken);

        ResponseEntity<ApiResponse<LoginResponse>> refreshResponse = restTemplate.exchange(
                "/api/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(refreshReq),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshResponse.getBody()).isNotNull();
        assertThat(refreshResponse.getBody().getData().getAccessToken()).isNotBlank();
    }
}
