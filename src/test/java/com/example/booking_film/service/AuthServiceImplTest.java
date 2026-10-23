package com.example.booking_film.service;

import com.example.booking_film.customexception.DuplicateResourceException;
import com.example.booking_film.dto.request.LoginRequest;
import com.example.booking_film.dto.request.RegisterRequest;
import com.example.booking_film.dto.response.LoginResponse;
import com.example.booking_film.dto.response.UserDTO;
import com.example.booking_film.entity.User;
import com.example.booking_film.enums.Role;
import com.example.booking_film.mapper.UserMapper;
import com.example.booking_film.repository.UserRepository;
import com.example.booking_film.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("register thành công khi email và số điện thoại chưa tồn tại")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Nguyễn Văn A");
        request.setEmail("a@example.com");
        request.setPassword("password123");
        request.setPhoneNumber("0912345678");

        User savedUser = User.builder()
                .id(1L)
                .fullName("Nguyễn Văn A")
                .email("a@example.com")
                .password("encodedPassword")
                .phoneNumber("0912345678")
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        UserDTO responseDTO = new UserDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("a@example.com");
        responseDTO.setFullName("Nguyễn Văn A");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(responseDTO);

        UserDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("a@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register ném DuplicateResourceException khi email đã tồn tại")
    void testRegister_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                authService.register(request)
        );
        assertEquals("Email đã tồn tại", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register ném DuplicateResourceException khi số điện thoại đã tồn tại")
    void testRegister_DuplicatePhoneNumber() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPhoneNumber("0912345678");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                authService.register(request)
        );
        assertEquals("Số điện thoại đã tồn tại", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login thành công trả về access token, refresh token và UserDTO")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        User principalUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .role(Role.CUSTOMER)
                .build();

        Authentication authenticated = mock(Authentication.class);
        when(authenticated.getPrincipal()).thenReturn(principalUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);
        when(jwtService.generateToken(authenticated)).thenReturn("mocked-access-token");
        when(jwtService.generateRefreshToken(authenticated)).thenReturn("mocked-refresh-token");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("user@example.com");
        when(userMapper.toDTO(principalUser)).thenReturn(userDTO);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-access-token", response.getAccessToken());
        assertEquals("mocked-refresh-token", response.getRefreshToken());
        assertEquals("user@example.com", response.getUser().getEmail());
    }

    @Test
    @DisplayName("login thất bại khi thông tin xác thực sai")
    void testLogin_BadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Sai tài khoản hoặc mật khẩu"));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(request)
        );
        verify(jwtService, never()).generateToken(any());
    }
}
