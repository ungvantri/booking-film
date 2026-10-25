package com.example.booking_film.integration;

import com.example.booking_film.dto.response.ApiResponse;
import com.example.booking_film.dto.response.CinemaDTO;
import com.example.booking_film.entity.Cinema;
import com.example.booking_film.entity.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinemaIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/cinemas trả về danh sách rạp từ MySQL container")
    void testGetAllCinemas_Success() {
        Cinema c1 = new Cinema();
        c1.setName("CGV Landmark");
        c1.setAddress("720A Điện Biên Phủ");
        c1.setCity("Hồ Chí Minh");
        c1.setPhoneNumber("0901234567");

        Cinema c2 = new Cinema();
        c2.setName("CGV Vincom");
        c2.setAddress("54A Nguyễn Chí Thanh");
        c2.setCity("Hà Nội");
        c2.setPhoneNumber("0907654321");

        cinemaRepository.saveAll(List.of(c1, c2));

        ResponseEntity<ApiResponse<List<CinemaDTO>>> response = restTemplate.exchange(
                "/api/cinemas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(2);
        assertThat(response.getBody().getData())
                .extracting(CinemaDTO::getName)
                .containsExactlyInAnyOrder("CGV Landmark", "CGV Vincom");
    }

    @Test
    @DisplayName("GET /api/cinemas/{id} trả về thông tin rạp tương ứng")
    void testGetCinemaById_Success() {
        Cinema c = new Cinema();
        c.setName("Lotte Cinema Go");
        c.setAddress("Quận 7");
        c.setCity("Hồ Chí Minh");
        c = cinemaRepository.save(c);

        ResponseEntity<ApiResponse<CinemaDTO>> response = restTemplate.exchange(
                "/api/cinemas/" + c.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("Lotte Cinema Go");
    }

    @Test
    @DisplayName("GET /api/cinemas/{id} với ID không tồn tại trả về 404 Not Found")
    void testGetCinemaById_NotFound() {
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/cinemas/99999",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    @DisplayName("POST /api/cinemas với quyền ADMIN phải tạo rạp mới thành công")
    void testCreateCinema_AsAdmin_Success() {
        CinemaDTO request = new CinemaDTO();
        request.setName("BHD Star Cineplex");
        request.setAddress("3/2 Phường 11");
        request.setCity("Hồ Chí Minh");
        request.setPhoneNumber("0283933333");

        HttpEntity<CinemaDTO> entity = new HttpEntity<>(request, createAdminHeaders());

        ResponseEntity<ApiResponse<CinemaDTO>> response = restTemplate.exchange(
                "/api/cinemas",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("BHD Star Cineplex");

        List<Cinema> allCinemas = cinemaRepository.findAll();
        assertThat(allCinemas).extracting(Cinema::getName).contains("BHD Star Cineplex");
    }

    @Test
    @DisplayName("PUT /api/cinemas/{id} phải cập nhật thông tin rạp thành công")
    void testUpdateCinema_Success() {
        Cinema c = new Cinema();
        c.setName("Galaxy Cinema Tân Bình");
        c.setAddress("Hoàng Hoa Thám");
        c.setCity("Hồ Chí Minh");
        c = cinemaRepository.save(c);

        CinemaDTO updateRequest = new CinemaDTO();
        updateRequest.setName("Galaxy Cinema Nguyễn Du");
        updateRequest.setAddress("116 Nguyễn Du");
        updateRequest.setCity("Hồ Chí Minh");
        updateRequest.setPhoneNumber("0283823523");

        HttpEntity<CinemaDTO> entity = new HttpEntity<>(updateRequest, createAdminHeaders());

        ResponseEntity<ApiResponse<CinemaDTO>> response = restTemplate.exchange(
                "/api/cinemas/" + c.getId(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("Galaxy Cinema Nguyễn Du");

        Cinema updated = cinemaRepository.findById(c.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Galaxy Cinema Nguyễn Du");
    }

    @Test
    @DisplayName("DELETE /api/cinemas/{id} phải xóa rạp thành công khi không có phòng chiếu")
    void testDeleteCinema_Success() {
        Cinema c = new Cinema();
        c.setName("Rạp Chiếu Cũ");
        c.setAddress("Đường X");
        c.setCity("Đà Nẵng");
        c = cinemaRepository.save(c);

        HttpEntity<Void> entity = new HttpEntity<>(createAdminHeaders());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/cinemas/" + c.getId(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cinemaRepository.findById(c.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/cinemas/{id} trả về 400 Bad Request khi rạp vẫn còn phòng chiếu")
    void testDeleteCinema_BadRequestWhenRoomsExist() {
        Cinema c = new Cinema();
        c.setName("Rạp Có Phòng Chiếu");
        c.setAddress("Đường Y");
        c.setCity("Hà Nội");
        c = cinemaRepository.save(c);

        Room r = new Room();
        r.setName("Phòng 01");
        r.setTotalSeats(100);
        r.setCinema(c);
        roomRepository.save(r);

        HttpEntity<Void> entity = new HttpEntity<>(createAdminHeaders());

        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                "/api/cinemas/" + c.getId(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Không thể xóa rạp");
        assertThat(cinemaRepository.findById(c.getId())).isPresent();
    }
}
