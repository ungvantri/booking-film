package com.example.booking_film.event;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.entity.Booking;
import com.example.booking_film.entity.Promotion;
import com.example.booking_film.enums.BookingStatus;
import com.example.booking_film.repository.BookingRepository;
import com.example.booking_film.repository.PromotionRepository;
import com.example.booking_film.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RedisKeyExpiredListener extends KeyExpirationEventMessageListener {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PromotionService promotionService;

    public RedisKeyExpiredListener(RedisMessageListenerContainer container) {
        super(container);
    }

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (!expiredKey.startsWith("booking_timeout:")) {
            return;
        }
        String[] parts = expiredKey.split(":");
        String bookingCode = parts[parts.length - 1];
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đặt phòng"));
        if (booking.getPromotion() != null) {
            Promotion promotion = booking.getPromotion();
            promotionService.decrementUsedCount(promotion.getId());
            promotion.setUsedCount(promotion.getUsedCount() - 1);
        }
        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);
    }
}
