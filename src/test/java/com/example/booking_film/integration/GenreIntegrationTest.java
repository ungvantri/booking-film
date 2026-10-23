package com.example.booking_film.integration;

import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.GenreDTO;
import com.example.booking_film.entity.Genre;
import com.example.booking_film.entity.Movie;
import com.example.booking_film.enums.AgeRating;
import com.example.booking_film.enums.MovieStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenreIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/genres phải trả về danh sách thể loại từ MySQL container")
    void testGetAllGenres_Success() {
        Genre g1 = new Genre();
        g1.setName("Hành động");
        Genre g2 = new Genre();
        g2.setName("Tình cảm");
        genreRepository.saveAll(List.of(g1, g2));

        ResponseEntity<ApiResponse<List<GenreDTO>>> response = restTemplate.exchange(
                "/api/genres",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData())
                .extracting(GenreDTO::getName)
                .containsExactlyInAnyOrder("Hành động", "Tình cảm");
    }

    @Test
    @DisplayName("POST /api/genres với quyền ADMIN phải lưu thành công thể loại vào database")
    void testCreateGenre_AsAdmin_Success() {
        GenreDTO request = new GenreDTO();
        request.setName("Khoa học viễn tưởng");

        HttpEntity<GenreDTO> entity = new HttpEntity<>(request, createAdminHeaders());

        ResponseEntity<ApiResponse<GenreDTO>> response = restTemplate.exchange(
                "/api/genres",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData().getName()).isEqualTo("Khoa học viễn tưởng");

        // Kiểm tra thực tế trong database
        List<Genre> genres = genreRepository.findAll();
        assertThat(genres).extracting(Genre::getName).contains("Khoa học viễn tưởng");
    }

    @Test
    @DisplayName("POST /api/genres không có token hoặc quyền USER phải trả về 401 hoặc 403")
    void testCreateGenre_Unauthorized_Or_Forbidden() {
        GenreDTO request = new GenreDTO();
        request.setName("Hoạt hình");

        // Không gửi token
        HttpEntity<GenreDTO> entityNoAuth = new HttpEntity<>(request);
        ResponseEntity<String> responseNoAuth = restTemplate.exchange(
                "/api/genres",
                HttpMethod.POST,
                entityNoAuth,
                String.class
        );
        assertThat(responseNoAuth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Gửi token quyền USER
        HttpEntity<GenreDTO> entityUser = new HttpEntity<>(request, createUserHeaders());
        ResponseEntity<String> responseForbidden = restTemplate.exchange(
                "/api/genres",
                HttpMethod.POST,
                entityUser,
                String.class
        );
        assertThat(responseForbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("POST /api/genres với tên trống phải trả về 400 Bad Request do validation")
    void testCreateGenre_ValidationError() {
        GenreDTO request = new GenreDTO();
        request.setName("");

        HttpEntity<GenreDTO> entity = new HttpEntity<>(request, createAdminHeaders());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/genres",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrors()).isNotEmpty();
    }

    @Test
    @DisplayName("PUT /api/genres/{id} phải cập nhật tên thể loại trong database")
    void testUpdateGenre_Success() {
        Genre genre = new Genre();
        genre.setName("Kinh dị cổ điển");
        genre = genreRepository.save(genre);

        GenreDTO updateRequest = new GenreDTO();
        updateRequest.setName("Kinh dị hiện đại");

        HttpEntity<GenreDTO> entity = new HttpEntity<>(updateRequest, createAdminHeaders());

        ResponseEntity<ApiResponse<GenreDTO>> response = restTemplate.exchange(
                "/api/genres/" + genre.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("Kinh dị hiện đại");

        Genre updatedGenre = genreRepository.findById(genre.getId()).orElseThrow();
        assertThat(updatedGenre.getName()).isEqualTo("Kinh dị hiện đại");
    }

    @Test
    @DisplayName("DELETE /api/genres/{id} phải xóa thể loại khỏi database khi chưa có phim nào dùng")
    void testDeleteGenre_Success() {
        Genre genre = new Genre();
        genre.setName("Tài liệu");
        genre = genreRepository.save(genre);

        HttpEntity<Void> entity = new HttpEntity<>(createAdminHeaders());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/genres/" + genre.getId(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(genreRepository.findById(genre.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/genres/{id} trả về 409 Conflict khi thể loại đang được phim sử dụng")
    void testDeleteGenre_ConflictWhenInUse() {
        Genre genre = new Genre();
        genre.setName("Hài hước");
        genre = genreRepository.save(genre);

        Movie movie = new Movie();
        movie.setTitle("Phim Hài 2026");
        movie.setDuration(120);
        movie.setReleaseDate(LocalDate.now());
        movie.setAgeRating(AgeRating.P);
        movie.setStatus(MovieStatus.NOW_SHOWING);
        movie.setGenres(List.of(genre));
        movieRepository.save(movie);

        HttpEntity<Void> entity = new HttpEntity<>(createAdminHeaders());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/genres/" + genre.getId(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(genreRepository.findById(genre.getId())).isPresent();
    }
}
