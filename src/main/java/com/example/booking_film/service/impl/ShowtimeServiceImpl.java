package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.request.ShowtimeRequest;
import com.example.booking_film.dto.response.PageResponse;
import com.example.booking_film.dto.response.SeatDTO;
import com.example.booking_film.dto.response.ShowtimeDTO;
import com.example.booking_film.entity.*;
import com.example.booking_film.enums.SeatStatus;
import com.example.booking_film.enums.ShowtimeStatus;
import com.example.booking_film.mapper.SeatMapper;
import com.example.booking_film.mapper.ShowtimeMapper;
import com.example.booking_film.repository.*;
import com.example.booking_film.service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {
    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ShowtimeMapper showtimeMapper;

    @Autowired
    private SeatShowtimeRepository seatShowtimeRepository;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    @Cacheable(value = "showtimes", key = "#movieId + '-' + #cinemaId + '-' + #date + '-' + #page + '-' + #size")
    public PageResponse<ShowtimeDTO> getAllShowtimes(Long movieId, Long cinemaId, LocalDate date, int page, int size) {
        LocalDateTime startOfDay = (date != null) ? date.atStartOfDay() : null;
        LocalDateTime endOfDay = (date != null) ? date.plusDays(1).atStartOfDay() : null;
        Pageable pageable = PageRequest.of(page, size);
        Page<Showtime> pages = showtimeRepository.filter(movieId, cinemaId, startOfDay, endOfDay, pageable);
        List<ShowtimeDTO> items = showtimeMapper.toDtoList(pages.getContent());
        return PageResponse.<ShowtimeDTO> builder()
                .items(items)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalItems(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .hasNext(pages.hasNext())
                .hasPrevious(pages.hasPrevious())
                .build();
    }

    @Override
    @Cacheable(value = "showtime_detail", key = "#id")
    public ShowtimeDTO getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không thế tìm thấy thông tin chiếu phim có id: " + id));
        return showtimeMapper.toDto(showtime);
    }

    @Override
    @Cacheable(value = "showtime_seats", key = "#showtimeId")
    public List<SeatDTO> getSeatsOfShowtime(Long showtimeId) {
        List<SeatShowtime> seats = seatShowtimeRepository.findByShowtimeId(showtimeId);
        List<SeatDTO> result = seats.stream().map(item -> seatMapper.toDTO(item.getSeat())).toList();
        return result;
    }

    @Override
    @CacheEvict(value = {"showtimes", "movie_showtimes"}, allEntries = true)
    public ShowtimeDTO createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim có id: " + request.getMovieId()));
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng có id: " + request.getRoomId()));
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());
        List<Showtime> showtimes = showtimeRepository.findByRoomId(request.getRoomId());
        if(showtimes.stream().anyMatch(item -> ((startTime.isBefore(item.getEndTime())) && (endTime.isAfter(item.getStartTime()))))) {
            throw new BadRequestException("Phòng chiếu đã có suất chiếu khác trong khung giờ này");
        }
        List<Seat> seats = seatRepository.findByRoomId(request.getRoomId());
        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(startTime)
                .endTime(endTime)
                .basePrice(request.getBasePrice())
                .status(ShowtimeStatus.SCHEDULED)
                .build();
        List<SeatShowtime> seatShowtimes = seats.stream().map(item -> SeatShowtime.builder().showtime(showtime).seat(item).status(SeatStatus.AVAILABLE).price(request.getBasePrice()).build()).collect(Collectors.toList());
        showtime.setSeatShowtimes(seatShowtimes);
        Showtime saved = showtimeRepository.save(showtime);
        return showtimeMapper.toDto(saved);
    }

    @Override
    @Caching(
            put = @CachePut(value = "showtime_detail", key = "#id"),
            evict = @CacheEvict(value = {"showtimes", "movie_showtimes", "showtime_seats"}, allEntries = true)
    )
    public ShowtimeDTO updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch chiếu có id: " + id));
        Movie movie = movieRepository.findById(request.getMovieId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim có id: " + request.getMovieId()));
        Room room = roomRepository.findById(request.getRoomId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng có id: " + request.getRoomId()));
        showtime.setBasePrice(request.getBasePrice());
        showtime.setStatus(request.getStatus());
        if(!bookingRepository.existsByShowtimeId(id)) {
            showtime.setMovie(movie);
            showtime.setRoom(room);
            showtime.setStartTime(request.getStartTime());
            showtime.setEndTime(request.getStartTime().plusMinutes(movie.getDuration()));
        }
        Showtime saved = showtimeRepository.save(showtime);
        return showtimeMapper.toDto(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "showtime_detail", key = "#id"),
            @CacheEvict(value = {"showtimes", "movie_showtimes", "showtime_seats"}, allEntries = true)
    })
    @Override
    public void cancelShowtime(Long id) {
        Showtime showtime = showtimeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất chiếu có id: " + id));
        showtime.setStatus(ShowtimeStatus.CANCELLED);
        showtimeRepository.save(showtime);
    }
}
