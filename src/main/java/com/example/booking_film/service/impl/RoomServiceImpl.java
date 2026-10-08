package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.ResourceInUseException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.RoomDTO;
import com.example.booking_film.entity.Cinema;
import com.example.booking_film.entity.Room;
import com.example.booking_film.entity.Seat;
import com.example.booking_film.enums.SeatType;
import com.example.booking_film.mapper.RoomMapper;
import com.example.booking_film.repository.CinemaRepository;
import com.example.booking_film.repository.RoomRepository;
import com.example.booking_film.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Override
    @Cacheable(value = "cinema_rooms", key = "#cinemaId")
    public List<RoomDTO> getRoomsByCinema(Long cinemaId) {
        if(!cinemaRepository.existsById(cinemaId)) {
            throw new ResourceNotFoundException("Không tìm thấy rạp phim có id " + cinemaId);
        }
        return roomMapper.toDTO(roomRepository.findByCinemaId(cinemaId));
    }

    @Override
    @Cacheable(value = "room_detail", key = "#id")
    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng có id " + id));
        return roomMapper.toDTO(room);
    }

    @Override
    @CacheEvict(value = "cinema_rooms", key = "#cinemaId")
    public RoomDTO createRoom(Long cinemaId, RoomDTO request) {
        Cinema cinema = cinemaRepository.findById(cinemaId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy rạp phim có id " + cinemaId));
        Room room = roomMapper.toEntity(request);
        room.setCinema(cinema);
        Room saved = roomRepository.save(room);
        return roomMapper.toDTO(saved);
    }

    @Override
    @Caching(
            put = @CachePut(value = "room_detail", key = "#id"),
            evict = @CacheEvict(value = "cinema_rooms", allEntries = true)
    )
    public RoomDTO updateRoom(Long id, RoomDTO request) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng có id " + id));
        Cinema cinema = cinemaRepository.findById(request.getCinemaId()).orElseThrow(() -> new ResourceNotFoundException("Thông tin cập nhật không hợp lệ"));
        room.setId(id);
        room.setName(request.getName());
        room.setType(request.getType());
        room.setTotalSeats(request.getTotalSeats());
        room.setCinema(cinema);
        Room saved = roomRepository.save(room);
        return roomMapper.toDTO(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "room_detail", key = "#id"),
            @CacheEvict(value = "cinema_rooms", allEntries = true)
    })
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong voi id: " + id));
        if (room.getShowtimes() != null && !room.getShowtimes().isEmpty()) {
            throw new ResourceInUseException("Phòng đang có suất chiếu, không thể xóa");
        }
        roomRepository.delete(room);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "room_detail", key = "#id"),
            @CacheEvict(value = "cinema_rooms", allEntries = true)
    })
    public void generateSeats(Long id, int totalRows, int totalColumns) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng có id: " + id));
        List<Seat> seats = new ArrayList<>();
        for(int i = 0; i < totalRows; i++) {
            char rowChar = (char) ('A' + i);
            String row = String.valueOf(rowChar);
            for(int j = 1; j <= totalColumns; j++) {
                Seat seat = new Seat();
                seat.setSeatCode(row + j);
                seat.setRowIndex(i + 1);
                seat.setColumnIndex(j);
                seat.setRoom(room);
                seat.setType(SeatType.NORMAL);
                seats.add(seat);
            }
        }
        room.setSeats(seats);
        room.setTotalSeats(totalColumns * totalRows);
        roomRepository.save(room);
    }
}
