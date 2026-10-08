package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.CinemaDTO;
import com.example.booking_film.entity.Cinema;
import com.example.booking_film.mapper.CinemaMapper;
import com.example.booking_film.repository.CinemaRepository;
import com.example.booking_film.service.CinemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CinemaServiceImpl implements CinemaService {
    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private CinemaMapper cinemaMapper;

    @Override
    @Cacheable(value = "cinemas", key = "#city == null ? 'all' : #city")
    public List<CinemaDTO> getAllCinemas(String city) {
        if(city == null) {
            return cinemaMapper.toListDTO(cinemaRepository.findAll());
        }
        return cinemaMapper.toListDTO(cinemaRepository.findByCity(city));
    }

    @Override
    @Cacheable(value = "cinema_detail", key = "#id")
    public CinemaDTO getCinemaById(Long id) {
        return cinemaMapper.toDTO(cinemaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với id: " + id)));
    }

    @Override
    @CacheEvict(value = "cinemas", allEntries = true)
    public CinemaDTO createCinema(CinemaDTO request) {
        Cinema cinema = cinemaMapper.toEntity(request);
        Cinema saved = cinemaRepository.save(cinema);
        return cinemaMapper.toDTO(saved);
    }

    @Override
    @Caching(
            put = @CachePut(value = "cinema_detail", key = "#id"),
            evict = @CacheEvict(value = "cinemas", allEntries = true)
    )
    public CinemaDTO updateCinema(CinemaDTO request, Long id) {
        Cinema cinema = cinemaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với id: " + id));
        Cinema cinemaUpdate = cinemaMapper.toEntity(request);
        cinemaUpdate.setId(id);
        cinemaUpdate.setRooms(cinema.getRooms());
        Cinema saved = cinemaRepository.save(cinemaUpdate);
        return cinemaMapper.toDTO(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "cinema_detail", key = "#id"),
            @CacheEvict(value = "cinemas", allEntries = true)
    })
    public void deleteCinema(Long id) {
        Cinema cinema = cinemaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy rạp chiếu phim có id: " + id));
        if(!cinema.getRooms().isEmpty()) {
            throw new BadRequestException("Không được xóa vì vẫn còn room");
        }
        cinemaRepository.deleteById(id);
    }

}
