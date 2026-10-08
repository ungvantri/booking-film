package com.example.booking_film.service.impl;

import com.example.booking_film.dto.request.SeatEventDTO;
import com.example.booking_film.service.SeatRealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeatRealtimeServiceImpl implements SeatRealtimeService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastSeatUpdate(SeatEventDTO event) {
        String destination = "/topic/showtimes/" + event.getShowtimeId() + "/seats";
        messagingTemplate.convertAndSend(destination, event);
    }
}
