package com.example.booking_film.controller;

import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.GenreDTO;
import com.example.booking_film.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {
    @Autowired
    private GenreService genreService;

    @GetMapping
    public ApiResponse<List<GenreDTO>> getAllGenres() {
        List<GenreDTO> genres = genreService.getAllGenres();
        return ApiResponse.<List<GenreDTO>>builder()
                .success(true)
                .message("Lấy danh sách thể loại thành công")
                .data(genres)
                .build();
    }

    @PostMapping
    public ApiResponse<GenreDTO> createGenre(@Valid @RequestBody GenreDTO request) {
        GenreDTO genreDTO = genreService.createGenre(request);
        return ApiResponse.<GenreDTO>builder()
                .success(true)
                .message("Tạo thể loại mới thành công")
                .data(genreDTO)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<GenreDTO> updateGenre(@PathVariable Long id, @Valid @RequestBody GenreDTO request) {
        GenreDTO genreDTO = genreService.updateGenre(id, request);
        return ApiResponse.<GenreDTO>builder()
                .success(true)
                .message("Cập nhật thể loại thành công")
                .data(genreDTO)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa thể loại thành công")
                .build();
    }
}
