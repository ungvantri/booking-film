package com.example.booking_film.controller;

import com.example.booking_film.dto.request.MovieRequest;
import com.example.booking_film.dto.response.*;
import com.example.booking_film.enums.MovieStatus;
import com.example.booking_film.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @GetMapping
    public ApiResponse<PageResponse<MovieDTO>> getMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        PageResponse<MovieDTO> result = movieService.getMovies(search, status, page, size, sortBy, direction);
        return ApiResponse.<PageResponse<MovieDTO>>builder()
                .success(true)
                .message("Lấy danh sách phim thành công")
                .data(result)
                .build();
    }

    @GetMapping("/now-showing")
    public ApiResponse<PageResponse<MovieDTO>> getNowShowingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<MovieDTO> result = movieService.getNowShowingMovies(page, size);
        return ApiResponse.<PageResponse<MovieDTO>>builder()
                .success(true)
                .message("Lấy danh sách phim thành công")
                .data(result)
                .build();
    }

    @GetMapping("/coming-soon")
    public ApiResponse<PageResponse<MovieDTO>> getComingSoonMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<MovieDTO> result = movieService.getComingSoonMovies(page, size);
        return ApiResponse.<PageResponse<MovieDTO>>builder()
                .success(true)
                .message("Lấy danh sách phim thành công")
                .data(result)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<MovieDetailDTO> getMovieById(@PathVariable Long id) {
        MovieDetailDTO result = movieService.getMovieById(id);
        return ApiResponse.<MovieDetailDTO>builder()
                .success(true)
                .message("Lấy danh sách phim thành công")
                .data(result)
                .build();
    }

    @GetMapping("/{id}/showtimes")
    public ApiResponse<List<CinemaShowtimeGroupDTO>> getShowtimesByMovie(
            @PathVariable Long id,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String city
    ) {
        List<CinemaShowtimeGroupDTO> result = movieService.getShowtimesByMovie(id, cinemaId, date, city);
        return ApiResponse.<List<CinemaShowtimeGroupDTO>>builder()
                .success(true)
                .message("Lấy danh sách phim thành công")
                .data(result)
                .build();
    }

    @PostMapping
    public ApiResponse<MovieDTO> createMovie(@Valid @RequestBody MovieRequest request){
        MovieDTO movieDTO = movieService.createMovie(request);
        return ApiResponse.<MovieDTO>builder()
                .success(true)
                .message("Tạo phim mới thành công")
                .data(movieDTO)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<MovieDTO> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequest request
    ) {
        MovieDTO movieDTO = movieService.updateMovie(id, request);
        return ApiResponse.<MovieDTO>builder()
                .success(true)
                .message("Cập nhật phim thành công")
                .data(movieDTO)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa phim thành công")
                .build();
    }

    @PutMapping("{id}/poster")
    public ApiResponse<String> uploadPoster(@PathVariable Long id, @RequestParam MultipartFile file) {
        String url = movieService.uploadPoster(id, file);
        return ApiResponse.<String>builder()
                .success(true)
                .message("Tải poster thành công")
                .data(url)
                .build();
    }
}
