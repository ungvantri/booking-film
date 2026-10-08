package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.ResourceInUseException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.SeatDTO;
import com.example.booking_film.entity.Seat;
import com.example.booking_film.mapper.SeatMapper;
import com.example.booking_film.repository.BookingSeatRepository;
import com.example.booking_film.repository.SeatRepository;
import com.example.booking_film.repository.SeatShowtimeRepository;
import com.example.booking_film.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatServiceImpl implements SeatService {
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private SeatShowtimeRepository seatShowtimeRepository;

    @Override
    @Cacheable(value = "room_seats", key = "#roomId")
    public List<SeatDTO> getSeatsByRoom(Long roomId) {
        List<Seat> seats = seatRepository.findByRoomId(roomId);
        return seatMapper.toDTO(seats);
    }

    @Override
    @CacheEvict(value = "room_seats", key = "#request.roomId")
    public SeatDTO updateSeat(Long id, SeatDTO request) {
        Seat seat = seatRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay ghe voi id: " + id));
        seat.setType(request.getType());
        Seat saved = seatRepository.save(seat);
        return seatMapper.toDTO(saved);
    }

    @Override
    @CacheEvict(value = "room_seats", allEntries = true)
    public void deleteSeat(Long seatId) {
        if(!bookingSeatRepository.findBySeatId(seatId).isEmpty()) {
            throw new ResourceInUseException("Không thế xóa vì ghế đã được đặt");
        }
        if(!seatShowtimeRepository.findBySeatId(seatId).isEmpty()) {
            throw new ResourceInUseException("Không thể xóa vì đã tồn tại trong danh sách chiếu");
        }
        seatRepository.deleteById(seatId);
    }
}
