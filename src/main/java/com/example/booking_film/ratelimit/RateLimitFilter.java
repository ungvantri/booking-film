package com.example.booking_film.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String clientIp = request.getRemoteAddr();
        RateLimitPlan plan = RateLimitPlan.resolvePlan(requestUri);
        ConsumptionProbe probe = rateLimitingService.tryConsume(clientIp, plan);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Retry-After", String.valueOf(waitSeconds));

        String jsonResponse = "{\"success\":false,\"message\":\"Quá nhiều yêu cầu từ IP của bạn. Vui lòng thử lại sau "
                + waitSeconds
                + " giây.\",\"data\":null,\"errors\":[\"RATE_LIMIT_EXCEEDED\"],\"timestamp\":\""
                + LocalDateTime.now()
                + "\"}";

        response.getWriter().write(jsonResponse);
    }
}
