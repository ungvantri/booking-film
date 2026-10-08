package com.example.booking_film.controller;

import com.example.booking_film.dto.request.ShowtimeRequest;
import com.example.booking_film.dto.response.*;
import com.example.booking_film.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {
    @Autowired
    private ShowtimeService showtimeService;

    @GetMapping
    public ApiResponse<PageResponse<ShowtimeDTO>> getAllShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<ShowtimeDTO> pages = showtimeService.getAllShowtimes(movieId, cinemaId, date, page, size);
        return ApiResponse.<PageResponse<ShowtimeDTO>>builder()
                .success(true)
                .message("Lấy danh sách thời gian chiếu thành công")
                .data(pages)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ShowtimeDTO> getShowtimeById(@PathVariable Long id) {
        ShowtimeDTO showtime = showtimeService.getShowtimeById(id);
        return ApiResponse.<ShowtimeDTO>builder()
                .success(true)
                .message("Lấy thông tin chiếu với id: " + id + " thành công")
                .data(showtime)
                .build();
    }

    @GetMapping("/{id}/seats")
    public ApiResponse<List<SeatDTO>> getSeatsOfShowtime(@PathVariable(name = "id") Long showtimeId) {
        List<SeatDTO> listSeat = showtimeService.getSeatsOfShowtime(showtimeId);
        return ApiResponse.<List<SeatDTO>>builder()
                .success(true)
                .message("Lấy danh sách ghế của phòng chiếu có id:" + showtimeId + " thành công")
                .data(listSeat)
                .build();
    }

    @PostMapping
    public ApiResponse<ShowtimeDTO> createShowtime(@Valid @RequestBody ShowtimeRequest request) {
        ShowtimeDTO showtime = showtimeService.createShowtime(request);
        return ApiResponse.<ShowtimeDTO>builder()
                .success(true)
                .message("Tạo xuất chiếu thành công")
                .data(showtime)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ShowtimeDTO> updateShowtime(@PathVariable Long id, @Valid @RequestBody ShowtimeRequest request) {
       ShowtimeDTO showtime = showtimeService.updateShowtime(id, request);
        return ApiResponse.<ShowtimeDTO>builder()
                .success(true)
                .message("Cập nhật xuất chiếu thành công")
                .data(showtime)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelShowtime(@PathVariable Long id) {
        showtimeService.cancelShowtime(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa xuất chiếu thành công")
                .build();
    }
}
