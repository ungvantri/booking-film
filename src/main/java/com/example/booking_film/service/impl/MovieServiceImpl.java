package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.request.MovieRequest;
import com.example.booking_film.dto.response.*;
import com.example.booking_film.entity.Genre;
import com.example.booking_film.entity.Movie;
import com.example.booking_film.entity.Showtime;
import com.example.booking_film.enums.MovieStatus;
import com.example.booking_film.mapper.MovieMapper;
import com.example.booking_film.repository.GenreRepository;
import com.example.booking_film.repository.MovieRepository;
import com.example.booking_film.service.CloudinaryService;
import com.example.booking_film.service.MovieService;
import com.example.booking_film.util.ShowtimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@Service
public class MovieServiceImpl implements MovieService {
    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ShowtimeUtil showtimeUtil;

    @Autowired
    private GenreRepository genreRepository;

    @Cacheable(value = "movies_filter", key = "#status + '-' + #page + '-' + #size + '-' + #sortBy + '-' + #direction")
    public PageResponse<MovieDTO> getMovies(String search, MovieStatus status, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Movie> pages = movieRepository.findByFilters(search, status, pageable);
        List<MovieDTO> items = movieMapper.toDtoList(pages.getContent());
        return PageResponse.<MovieDTO> builder()
                .items(items)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalItems(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .hasNext(pages.hasNext())
                .hasPrevious(pages.hasPrevious())
                .build();
    }

    @Cacheable(value = "movies_now_showing", key = "#page + '-' + #size")
    public PageResponse<MovieDTO> getNowShowingMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> pages = movieRepository.findByStatus(MovieStatus.NOW_SHOWING, pageable);
        List<MovieDTO> items = movieMapper.toDtoList(pages.getContent());
        return PageResponse.<MovieDTO> builder()
                .items(items)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalItems(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .hasNext(pages.hasNext())
                .hasPrevious(pages.hasPrevious())
                .build();
    }

    @Cacheable(value = "movies_coming_soon", key = "#page + '-' + #size")
    public PageResponse<MovieDTO> getComingSoonMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> pages = movieRepository.findByStatus(MovieStatus.COMING_SOON, pageable);
        List<MovieDTO> items = movieMapper.toDtoList(pages.getContent());
        return PageResponse.<MovieDTO> builder()
                .items(items)
                .currentPage(pages.getNumber())
                .pageSize(pages.getSize())
                .totalItems(pages.getTotalElements())
                .totalPages(pages.getTotalPages())
                .hasNext(pages.hasNext())
                .hasPrevious(pages.hasPrevious())
                .build();
    }

    @Cacheable(value = "movie_detail", key = "#id")
    public MovieDetailDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung co id:" + id));
        MovieDTO movieDTO = movieMapper.toDto(movie);
        List<Showtime> showtimes = movie.getShowtimes();
        List<CinemaShowtimeGroupDTO> cinemaShowtimeGroupDTOS = showtimeUtil.groupShowtimeByCinema(showtimes);
        return new MovieDetailDTO(movieDTO, cinemaShowtimeGroupDTOS);
    }

    @Cacheable(value = "movie_showtimes", key = "#id + '-' + #cinemaId + '-' + #date + '-' + #city")
    public List<CinemaShowtimeGroupDTO> getShowtimesByMovie(Long id, Long cinemaId, LocalDate date, String city) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim có id: " + id));
        List<Showtime> filtered = movie.getShowtimes().stream()
                .filter(st -> date == null || st.getStartTime().toLocalDate().equals(date))
                .filter(st -> city == null || st.getRoom().getCinema().getCity().equalsIgnoreCase(city))
                .filter(st -> cinemaId == null || st.getRoom().getCinema().getId().equals(cinemaId))
                .toList();
        return showtimeUtil.groupShowtimeByCinema(filtered);
    }

    @CacheEvict(value = {"movies_now_showing", "movies_coming_soon", "movies_filter"}, allEntries = true)
    public MovieDTO createMovie(MovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        List<Genre> genres = request.getGenreIds().stream().map(id -> genreRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy genre có id: " + id))).toList();
        movie.setGenres(genres);
        Movie saved = movieRepository.save(movie);
        return movieMapper.toDto(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "movie_detail", key = "#id"),
            @CacheEvict(value = {"movies_now_showing", "movies_coming_soon", "movies_filter"}, allEntries = true)
    })
    public MovieDTO updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tồn tại phim có id: " + id));
        Movie updatedMovie = movieMapper.toEntity(request);
        updatedMovie.setId(id);
        updatedMovie.setRating(movie.getRating());
        updatedMovie.setGenres(movie.getGenres());
        updatedMovie.setShowtimes(movie.getShowtimes());
        updatedMovie.setCreatedAt(movie.getCreatedAt());
        Movie saved = movieRepository.save(updatedMovie);
        return movieMapper.toDto(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "movie_detail", key = "#id"),
            @CacheEvict(value = {"movies_now_showing", "movies_coming_soon", "movies_filter"}, allEntries = true)
    })
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim có id: " + id));
        if(!movie.getShowtimes().isEmpty()) {
            throw new BadRequestException("Không được xóa movie vì vẫn còn có xuất chiếu");
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "movie_detail", key = "#id"),
            @CacheEvict(value = {"movies_now_showing", "movies_coming_soon", "movies_filter"}, allEntries = true)
    })
    public String uploadPoster(Long id, MultipartFile file) {
        String url = cloudinaryService.uploadPoster(file);
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim có id: " + id));
        movie.setPosterUrl(url);
        movieRepository.save(movie);
        return url;
    }
}
