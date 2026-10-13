package com.example.booking_film.config;

import com.example.booking_film.ratelimit.RateLimitFilter;
import com.example.booking_film.service.UserDetailServiceCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] ADMIN_MANAGED_RESOURCES = {
            "/api/movies/**", "/api/genres/**", "/api/cinemas/**",
            "/api/rooms/**", "/api/seats/**", "/api/showtimes/**"
    };

    @Autowired
    private UserDetailServiceCustom userDetailsServiceCustom;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/payments/vnpay/callback").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/movies/**", "/api/genres/**", "/api/cinemas/**",
                                "/api/showtimes/**", "/api/promotions/active").permitAll()
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/promotions/validate").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/payments").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings").hasAnyAuthority("SCOPE_ADMIN", "SCOPE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/code/*").hasAnyAuthority("SCOPE_ADMIN", "SCOPE_STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/checkin").hasAnyAuthority("SCOPE_ADMIN", "SCOPE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/payments/*").hasAnyAuthority("SCOPE_ADMIN", "SCOPE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").hasAnyAuthority("SCOPE_ADMIN", "SCOPE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/refund").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/api/users/**").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/promotions").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/promotions").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/promotions/**").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/promotions/**").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.POST, ADMIN_MANAGED_RESOURCES).hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, ADMIN_MANAGED_RESOURCES).hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, ADMIN_MANAGED_RESOURCES).hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/seats/hold").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsServiceCustom);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(daoAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}