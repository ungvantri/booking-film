package com.example.booking_film.service;

import com.example.booking_film.customexception.ResourceInUseException;
import com.example.booking_film.customexception.ResourceNotFoundException;
import com.example.booking_film.dto.response.GenreDTO;
import com.example.booking_film.entity.Genre;
import com.example.booking_film.entity.Movie;
import com.example.booking_film.mapper.GenreMapper;
import com.example.booking_film.repository.GenreRepository;
import com.example.booking_film.service.impl.GenreServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreMapper genreMapper;

    @InjectMocks
    private GenreServiceImpl genreService;

    @Test
    @DisplayName("getAllGenres phải trả về danh sách GenreDTO")
    void testGetAllGenres_Success() {
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Hành động");

        GenreDTO dto = new GenreDTO();
        dto.setId(1L);
        dto.setName("Hành động");

        when(genreRepository.findAll()).thenReturn(List.of(genre));
        when(genreMapper.toDTO(List.of(genre))).thenReturn(List.of(dto));

        List<GenreDTO> result = genreService.getAllGenres();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hành động", result.get(0).getName());
        verify(genreRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createGenre phải lưu và trả về GenreDTO đã tạo")
    void testCreateGenre_Success() {
        GenreDTO request = new GenreDTO();
        request.setName("Kinh dị");

        Genre entity = new Genre();
        entity.setName("Kinh dị");

        Genre savedEntity = new Genre();
        savedEntity.setId(2L);
        savedEntity.setName("Kinh dị");

        GenreDTO responseDTO = new GenreDTO();
        responseDTO.setId(2L);
        responseDTO.setName("Kinh dị");

        when(genreMapper.toEntity(request)).thenReturn(entity);
        when(genreRepository.save(entity)).thenReturn(savedEntity);
        when(genreMapper.toDTO(savedEntity)).thenReturn(responseDTO);

        GenreDTO result = genreService.createGenre(request);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Kinh dị", result.getName());
        assertNull(entity.getId(), "Id entity phải được đặt về null trước khi lưu");
        verify(genreRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("updateGenre thành công khi id tồn tại")
    void testUpdateGenre_Success() {
        Long genreId = 1L;
        GenreDTO request = new GenreDTO();
        request.setName("Hài hước");

        Genre existingGenre = new Genre();
        existingGenre.setId(genreId);
        existingGenre.setName("Hành động");

        Genre savedGenre = new Genre();
        savedGenre.setId(genreId);
        savedGenre.setName("Hài hước");

        GenreDTO responseDTO = new GenreDTO();
        responseDTO.setId(genreId);
        responseDTO.setName("Hài hước");

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.save(existingGenre)).thenReturn(savedGenre);
        when(genreMapper.toDTO(savedGenre)).thenReturn(responseDTO);

        GenreDTO result = genreService.updateGenre(genreId, request);

        assertNotNull(result);
        assertEquals("Hài hước", result.getName());
        assertEquals("Hài hước", existingGenre.getName());
        verify(genreRepository, times(1)).save(existingGenre);
    }

    @Test
    @DisplayName("updateGenre ném ResourceNotFoundException khi id không tồn tại")
    void testUpdateGenre_NotFound() {
        Long genreId = 999L;
        GenreDTO request = new GenreDTO();
        request.setName("Hoạt hình");

        when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                genreService.updateGenre(genreId, request)
        );
        verify(genreRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteGenre thành công khi không có phim nào dùng thể loại này")
    void testDeleteGenre_Success() {
        Long genreId = 1L;
        Genre genre = new Genre();
        genre.setId(genreId);
        genre.setMovies(Collections.emptyList());

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

        genreService.deleteGenre(genreId);

        verify(genreRepository, times(1)).deleteById(genreId);
    }

    @Test
    @DisplayName("deleteGenre ném ResourceInUseException khi vẫn còn phim thuộc thể loại này")
    void testDeleteGenre_InUse() {
        Long genreId = 1L;
        Genre genre = new Genre();
        genre.setId(genreId);
        genre.setMovies(List.of(new Movie()));

        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

        assertThrows(ResourceInUseException.class, () ->
                genreService.deleteGenre(genreId)
        );
        verify(genreRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteGenre ném ResourceNotFoundException khi id không tồn tại")
    void testDeleteGenre_NotFound() {
        Long genreId = 999L;
        when(genreRepository.findById(genreId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                genreService.deleteGenre(genreId)
        );
        verify(genreRepository, never()).deleteById(anyLong());
    }
}
