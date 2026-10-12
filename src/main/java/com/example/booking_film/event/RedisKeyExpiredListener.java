package com.example.booking_film.event;

import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.entity.Booking;
import com.example.booking_film.enums.BookingStatus;
import com.example.booking_film.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

public class RedisKeyExpiredListener extends KeyExpirationEventMessageListener {
    @Autowired
    private BookingRepository bookingRepository;

    public RedisKeyExpiredListener(RedisMessageListenerContainer container) {
        super(container);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        String[] parts = expiredKey.split(":");
        String bookingCode = parts[parts.length - 1];
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đặt phòng"));
        booking.setStatus(BookingStatus.EXPIRED);
    }
}
