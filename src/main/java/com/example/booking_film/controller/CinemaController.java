package com.example.booking_film.controller;

import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.CinemaDTO;
import com.example.booking_film.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
public class CinemaController {
    @Autowired
    private CinemaService cinemaService;

    @GetMapping
    public ApiResponse<List<CinemaDTO>> getAllCinemas(@RequestParam(required = false) String city) {
        List<CinemaDTO> cinemas = cinemaService.getAllCinemas(city);
        return ApiResponse.<List<CinemaDTO>>builder()
                .success(true)
                .message("Lấy danh sách các rạp chiếu phim thành công")
                .data(cinemas)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CinemaDTO> getCinemaById(@PathVariable Long id) {
        CinemaDTO cinema = cinemaService.getCinemaById(id);
        return ApiResponse.<CinemaDTO>builder()
                .success(true)
                .message("Lấy rạp phim theo yêu cầu thành công")
                .data(cinema)
                .build();
    }

    @PostMapping
    public ApiResponse<CinemaDTO> createCinema(@RequestBody CinemaDTO request) {
        CinemaDTO cinema = cinemaService.createCinema(request);
        return ApiResponse.<CinemaDTO>builder()
                .success(true)
                .message("Tạo rạp phim thành công")
                .data(cinema)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CinemaDTO> updateCinema(@PathVariable Long id, @RequestBody CinemaDTO request) {
        CinemaDTO cinema = cinemaService.updateCinema(request, id);
        return ApiResponse.<CinemaDTO>builder()
                .success(true)
                .message("Cập nhật thông tin rạp phim thành công")
                .data(cinema)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCinema(@PathVariable Long id) {
        cinemaService.deleteCinema(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa rạp thành công")
                .build();
    }
}
