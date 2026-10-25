package com.example.booking_film.service;

import com.example.booking_film.customexception.ResourceInUseException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.CinemaDTO;
import com.example.booking_film.entity.Cinema;
import com.example.booking_film.entity.Room;
import com.example.booking_film.mapper.CinemaMapper;
import com.example.booking_film.repository.CinemaRepository;
import com.example.booking_film.service.impl.CinemaServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceImplTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private CinemaMapper cinemaMapper;

    @InjectMocks
    private CinemaServiceImpl cinemaService;

    @Test
    @DisplayName("getAllCinemas với city=null phải trả về tất cả rạp")
    void testGetAllCinemas_All() {
        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("CGV Aeon");

        CinemaDTO dto = new CinemaDTO();
        dto.setId(1L);
        dto.setName("CGV Aeon");

        when(cinemaRepository.findAll()).thenReturn(List.of(cinema));
        when(cinemaMapper.toListDTO(List.of(cinema))).thenReturn(List.of(dto));

        List<CinemaDTO> result = cinemaService.getAllCinemas(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CGV Aeon", result.get(0).getName());
        verify(cinemaRepository, times(1)).findAll();
        verify(cinemaRepository, never()).findByCity(any());
    }

    @Test
    @DisplayName("getAllCinemas với city cụ thể phải lọc theo city")
    void testGetAllCinemas_ByCity() {
        String city = "Hà Nội";
        Cinema cinema = new Cinema();
        cinema.setId(2L);
        cinema.setCity(city);

        CinemaDTO dto = new CinemaDTO();
        dto.setId(2L);
        dto.setCity(city);

        when(cinemaRepository.findByCity(city)).thenReturn(List.of(cinema));
        when(cinemaMapper.toListDTO(List.of(cinema))).thenReturn(List.of(dto));

        List<CinemaDTO> result = cinemaService.getAllCinemas(city);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(city, result.get(0).getCity());
        verify(cinemaRepository, times(1)).findByCity(city);
        verify(cinemaRepository, never()).findAll();
    }

    @Test
    @DisplayName("getCinemaById thành công khi id tồn tại")
    void testGetCinemaById_Success() {
        Long id = 1L;
        Cinema cinema = new Cinema();
        cinema.setId(id);
        cinema.setName("BHD Star");

        CinemaDTO dto = new CinemaDTO();
        dto.setId(id);
        dto.setName("BHD Star");

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(cinema));
        when(cinemaMapper.toDTO(cinema)).thenReturn(dto);

        CinemaDTO result = cinemaService.getCinemaById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("BHD Star", result.getName());
    }

    @Test
    @DisplayName("getCinemaById ném ResourceNotFoundException khi id không tồn tại")
    void testGetCinemaById_NotFound() {
        Long id = 999L;
        when(cinemaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cinemaService.getCinemaById(id)
        );
    }

    @Test
    @DisplayName("createCinema thành công")
    void testCreateCinema_Success() {
        CinemaDTO request = new CinemaDTO();
        request.setName("Lotte Cinema");

        Cinema entity = new Cinema();
        entity.setName("Lotte Cinema");

        Cinema saved = new Cinema();
        saved.setId(5L);
        saved.setName("Lotte Cinema");

        CinemaDTO response = new CinemaDTO();
        response.setId(5L);
        response.setName("Lotte Cinema");

        when(cinemaMapper.toEntity(request)).thenReturn(entity);
        when(cinemaRepository.save(entity)).thenReturn(saved);
        when(cinemaMapper.toDTO(saved)).thenReturn(response);

        CinemaDTO result = cinemaService.createCinema(request);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertNull(entity.getId(), "Id của entity phải được set về null trước khi lưu");
        verify(cinemaRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("updateCinema thành công")
    void testUpdateCinema_Success() {
        Long id = 1L;
        CinemaDTO request = new CinemaDTO();
        request.setName("CGV Metropolis Mới");
        request.setAddress("29 Liễu Giai");
        request.setCity("Hà Nội");
        request.setPhoneNumber("0912345678");

        Cinema existing = new Cinema();
        existing.setId(id);
        existing.setName("CGV Cũ");

        Cinema saved = new Cinema();
        saved.setId(id);
        saved.setName("CGV Metropolis Mới");

        CinemaDTO response = new CinemaDTO();
        response.setId(id);
        response.setName("CGV Metropolis Mới");

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(existing));
        when(cinemaRepository.save(existing)).thenReturn(saved);
        when(cinemaMapper.toDTO(saved)).thenReturn(response);

        CinemaDTO result = cinemaService.updateCinema(request, id);

        assertNotNull(result);
        assertEquals("CGV Metropolis Mới", result.getName());
        assertEquals("CGV Metropolis Mới", existing.getName());
        assertEquals("29 Liễu Giai", existing.getAddress());
        assertEquals("0912345678", existing.getPhoneNumber());
        verify(cinemaRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("deleteCinema thành công khi rạp không có phòng chiếu")
    void testDeleteCinema_Success() {
        Long id = 1L;
        Cinema cinema = new Cinema();
        cinema.setId(id);
        cinema.setRooms(Collections.emptyList());

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(cinema));

        cinemaService.deleteCinema(id);

        verify(cinemaRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deleteCinema ném ResourceInUseException khi rạp vẫn còn phòng chiếu")
    void testDeleteCinema_HasRooms() {
        Long id = 1L;
        Cinema cinema = new Cinema();
        cinema.setId(id);
        cinema.setRooms(List.of(new Room()));

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(cinema));

        assertThrows(ResourceInUseException.class, () ->
                cinemaService.deleteCinema(id)
        );
        verify(cinemaRepository, never()).deleteById(anyLong());
    }
}
