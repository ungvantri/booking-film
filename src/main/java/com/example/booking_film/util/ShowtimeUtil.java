package com.example.booking_film.util;

import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.CinemaShowtimeGroupDTO;
import com.example.booking_film.entity.Cinema;
import com.example.booking_film.entity.Showtime;
import com.example.booking_film.mapper.ShowtimeMapper;
import com.example.booking_film.repository.CinemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ShowtimeUtil {
    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private ShowtimeMapper showtimeMapper;

    public List<CinemaShowtimeGroupDTO> groupShowtimeByCinema(List<Showtime> showtimes) {
        List<CinemaShowtimeGroupDTO> cinemaShowtimeGroupDTOS = new ArrayList<>();
        Map<Long, List<Showtime>> group = showtimes.stream().collect(Collectors.groupingBy(item -> item.getRoom().getCinema().getId()));
        group.forEach((key, value) -> {
            Cinema cinema = cinemaRepository.findById(key).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin phim"));
            CinemaShowtimeGroupDTO cinemaShowtimeGroupDTO = CinemaShowtimeGroupDTO.builder()
                    .cinemaId(key)
                    .cinemaName(cinema.getName())
                    .showtimes(showtimeMapper.toDtoList(value))
                    .build();
            cinemaShowtimeGroupDTOS.add(cinemaShowtimeGroupDTO);
        });
        return cinemaShowtimeGroupDTOS;
    }
}
