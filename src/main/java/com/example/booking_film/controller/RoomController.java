package com.example.booking_film.controller;

import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.RoomDTO;
import com.example.booking_film.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/api/cinemas/{cinemaId}/rooms")
    public ApiResponse<List<RoomDTO>> getRoomsByCinema(@PathVariable Long cinemaId) {
        List<RoomDTO> rooms = roomService.getRoomsByCinema(cinemaId);
        return ApiResponse.<List<RoomDTO>>builder()
                .success(true)
                .message("Lấy danh sách phòng thành công")
                .data(rooms)
                .build();
    }

    @GetMapping("/api/rooms/{id}")
    public ApiResponse<RoomDTO> getRoomById(@PathVariable Long id) {
        RoomDTO room = roomService.getRoomById(id);
        return ApiResponse.<RoomDTO>builder()
                .success(true)
                .message("Lấy phòng thành công")
                .data(room)
                .build();
    }

    @PostMapping("/api/cinemas/{cinemaId}/rooms")
    public ApiResponse<RoomDTO> createRoom(@PathVariable Long cinemaId, @Valid @RequestBody RoomDTO request) {
        RoomDTO room = roomService.createRoom(cinemaId, request);
        return ApiResponse.<RoomDTO>builder()
                .success(true)
                .message("Tạo phòng thành công")
                .data(room)
                .build();
    }

    @PutMapping("/api/rooms/{id}")
    public ApiResponse<RoomDTO> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDTO request) {
        RoomDTO room = roomService.updateRoom(id, request);
        return ApiResponse.<RoomDTO>builder()
                .success(true)
                .message("Cập nhật phòng thành công")
                .data(room)
                .build();
    }

    @DeleteMapping("/api/rooms/{id}")
    public ApiResponse<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa phòng thành công")
                .build();
    }

    @PostMapping("/api/rooms/{id}/seats/generate")
    public ApiResponse<Void> generateSeats(@PathVariable Long id, @RequestParam int totalRows, @RequestParam int totalColumns) {
        roomService.generateSeats(id, totalRows, totalColumns);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Tạo ghế ngồi cho phòng có id " + id + " thành công")
                .build();
    }
}
