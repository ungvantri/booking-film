package com.example.booking_film.controller;

import com.example.booking_film.dto.request.SeatEventDTO;
import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.SeatDTO;
import com.example.booking_film.service.SeatRealtimeService;
import com.example.booking_film.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SeatController {
    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatRealtimeService seatRealtimeService;

    @GetMapping("/api/rooms/{roomId}/seats")
    public ApiResponse<List<SeatDTO>> getSeatsByRoom(@PathVariable Long roomId) {
        List<SeatDTO> seats = seatService.getSeatsByRoom(roomId);
        return ApiResponse.<List<SeatDTO>>builder()
                .success(true)
                .message("Lấy danh sách ghế thành công")
                .data(seats)
                .build();
    }

    @PutMapping("/api/seats/{id}")
    public ApiResponse<SeatDTO> updateSeat(@PathVariable Long id, @RequestBody SeatDTO request) {
        SeatDTO seat = seatService.updateSeat(id, request);
        return ApiResponse.<SeatDTO>builder()
                .success(true)
                .message("Cập nhật ghế thành công")
                .data(seat)
                .build();
    }

    @DeleteMapping("/api/seats/{id}")
    public ApiResponse<Void> deleteSeat(@PathVariable(name = "id") Long seatId) {
        seatService.deleteSeat(seatId);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa ghế thành công")
                .build();
    }

    @PostMapping("/api/seats/hold")
    public ApiResponse<Void> holdSeat(@RequestBody SeatEventDTO request) {
        seatRealtimeService.broadcastSeatUpdate(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Cập nhật thành công")
                .build();
    }
}
