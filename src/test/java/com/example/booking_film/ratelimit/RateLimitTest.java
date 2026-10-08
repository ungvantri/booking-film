package com.example.booking_film.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    @DisplayName("Nên cho phép các request trong giới hạn và chặn khi vượt quá quota của IP")
    void testRateLimitByIp() {
        String testIp = "192.168.1.10";

        // Gói AUTH có giới hạn 15 request/phút
        for (int i = 0; i < 15; i++) {
            ConsumptionProbe probe = rateLimitingService.tryConsume(testIp, RateLimitPlan.AUTH);
            assertTrue(probe.isConsumed(), "Request thứ " + (i + 1) + " phải thành công");
        }

        // Request thứ 16 từ cùng IP đó phải bị từ chối
        ConsumptionProbe blockedProbe = rateLimitingService.tryConsume(testIp, RateLimitPlan.AUTH);
        assertFalse(blockedProbe.isConsumed(), "Request thứ 16 từ IP này phải bị từ chối");
        assertEquals(0, blockedProbe.getRemainingTokens());

        // Một IP khác hoàn toàn độc lập và không bị ảnh hưởng
        String anotherIp = "192.168.1.20";
        ConsumptionProbe anotherProbe = rateLimitingService.tryConsume(anotherIp, RateLimitPlan.AUTH);
        assertTrue(anotherProbe.isConsumed(), "IP khác phải có bucket riêng và gửi request thành công");
        assertEquals(14, anotherProbe.getRemainingTokens());
    }
}
