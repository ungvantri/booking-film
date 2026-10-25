package com.example.booking_film;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BookingFilmApplicationTests {

    @MockitoBean
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Test
    void contextLoads() {
    }

}

