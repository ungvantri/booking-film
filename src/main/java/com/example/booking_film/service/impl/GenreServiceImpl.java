package com.example.booking_film.service.impl;

import com.example.booking_film.customexception.BadRequestException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.GenreDTO;
import com.example.booking_film.entity.Genre;
import com.example.booking_film.mapper.GenreMapper;
import com.example.booking_film.repository.GenreRepository;
import com.example.booking_film.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenreServiceImpl implements GenreService {
    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private GenreMapper genreMapper;

    @Cacheable(value = "genres", key = "'all'")
    public List<GenreDTO> getAllGenres() {
        List<Genre> genres = genreRepository.findAll();
        return genreMapper.toDTO(genres);
    }

    @Override
    @CacheEvict(value = "genres", allEntries = true)
    public void createGenre(GenreDTO request) {
        Genre genre = genreMapper.toEntity(request);
        genreRepository.save(genre);
    }

    @Override
    @CacheEvict(value = "genres", allEntries = true)
    public void updateGenre(Long id, GenreDTO request) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thể loại phim có id: " + id));
        genre.setName(request.getName());
        genreRepository.save(genre);
    }

    @Override
    @CacheEvict(value = "genres", allEntries = true)
    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thể loại phim có id: " + id));
        if(!genre.getMovies().isEmpty()) {
            throw new BadRequestException("Không được xóa vì vẫn còn tồn tại phim có thể loại này");
        }
        genreRepository.deleteById(id);
    }
}
