package com.example.booking_film.integration;

import com.example.booking_film.entity.User;
import com.example.booking_film.enums.Role;
import com.example.booking_film.repository.*;
import com.example.booking_film.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(DockerAvailableCondition.class)
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("booking_film_test");

    @MockitoBean
    protected RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected GenreRepository genreRepository;

    @Autowired
    protected CinemaRepository cinemaRepository;

    @Autowired
    protected RoomRepository roomRepository;

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    protected void cleanDatabase() {
        try {
            movieRepository.deleteAll();
            genreRepository.deleteAll();
            roomRepository.deleteAll();
            cinemaRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception ignored) {
        }
    }

    protected HttpHeaders createAdminHeaders() {
        User admin = userRepository.findByEmail("admin_it@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .fullName("Admin Integration Test")
                        .email("admin_it@example.com")
                        .password(passwordEncoder.encode("AdminPass123!"))
                        .role(Role.ADMIN)
                        .isActive(true)
                        .build()));

        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
        String token = jwtService.generateToken(auth);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders createUserHeaders() {
        User user = userRepository.findByEmail("user_it@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .fullName("User Integration Test")
                        .email("user_it@example.com")
                        .password(passwordEncoder.encode("UserPass123!"))
                        .role(Role.CUSTOMER)
                        .isActive(true)
                        .build()));

        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        String token = jwtService.generateToken(auth);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
