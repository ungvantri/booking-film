package com.example.booking_film.util;

import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.CinemaShowtimeGroupDTO;
import com.example.booking_film.dto.response.ShowtimeDTO;
import com.example.booking_film.entity.Cinema;
import com.example.booking_film.entity.Room;
import com.example.booking_film.entity.Showtime;
import com.example.booking_film.mapper.ShowtimeMapper;
import com.example.booking_film.repository.CinemaRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowtimeUtilTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private ShowtimeMapper showtimeMapper;

    @InjectMocks
    private ShowtimeUtil showtimeUtil;

    @Test
    @DisplayName("groupShowtimeByCinema phải trả về danh sách rỗng khi input rỗng")
    void testGroupShowtimeByCinema_EmptyList() {
        List<CinemaShowtimeGroupDTO> result = showtimeUtil.groupShowtimeByCinema(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("groupShowtimeByCinema phải nhóm đúng các suất chiếu theo rạp")
    void testGroupShowtimeByCinema_Success() {
        // Mock Cinema
        Cinema cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("CGV Vincom");

        // Mock Room
        Room room = new Room();
        room.setId(10L);
        room.setCinema(cinema);

        // Mock Showtime
        Showtime showtime = Showtime.builder()
                .id(100L)
                .room(room)
                .build();

        ShowtimeDTO showtimeDTO = new ShowtimeDTO();
        showtimeDTO.setId(100L);

        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(showtimeMapper.toDtoList(anyList())).thenReturn(List.of(showtimeDTO));

        List<CinemaShowtimeGroupDTO> result = showtimeUtil.groupShowtimeByCinema(List.of(showtime));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCinemaId());
        assertEquals("CGV Vincom", result.get(0).getCinemaName());
        assertEquals(1, result.get(0).getShowtimes().size());
    }

    @Test
    @DisplayName("groupShowtimeByCinema phải ném ResourceNotFoundException nếu rạp không tồn tại")
    void testGroupShowtimeByCinema_CinemaNotFound() {
        Cinema cinema = new Cinema();
        cinema.setId(99L);

        Room room = new Room();
        room.setCinema(cinema);

        Showtime showtime = Showtime.builder()
                .id(100L)
                .room(room)
                .build();

        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                showtimeUtil.groupShowtimeByCinema(List.of(showtime))
        );
    }
}
