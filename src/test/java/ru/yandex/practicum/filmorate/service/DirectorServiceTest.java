package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {

    @Mock
    private DirectorStorage directorStorage;

    @InjectMocks
    private DirectorService directorService;

    private Director director;
    private DirectorDto directorDto;

    @BeforeEach
    void setUp() {
        director = new Director(1L, "Christopher Nolan");
        directorDto = new DirectorDto(1L, "Christopher Nolan");
    }

    @Test
    void getDirectors_ShouldReturnList() {
        when(directorStorage.getDirectors()).thenReturn(List.of(director));

        List<DirectorDto> result = directorService.getDirectors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Christopher Nolan");

        verify(directorStorage).getDirectors();
    }

    @Test
    void getDirectorById_ShouldReturnDirector() {
        when(directorStorage.findById(1L)).thenReturn(Optional.of(director));

        DirectorDto result = directorService.getDirectorById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Christopher Nolan");

        verify(directorStorage).findById(1L);
    }

    @Test
    void getDirectorById_NotFound_ShouldThrowException() {
        when(directorStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> directorService.getDirectorById(999L));

        verify(directorStorage).findById(999L);
    }

    @Test
    void addDirector_ShouldReturnSavedDirector() {
        DirectorDto inputDto = new DirectorDto(null, "Quentin Tarantino");
        Director savedDirector = new Director(2L, "Quentin Tarantino");

        when(directorStorage.addDirector(any(Director.class))).thenReturn(savedDirector);

        DirectorDto result = directorService.addDirector(inputDto);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Quentin Tarantino");

        verify(directorStorage).addDirector(any(Director.class));
    }

    @Test
    void updateDirector_ShouldReturnUpdatedDirector() {
        DirectorUpdateDto updateDto = new DirectorUpdateDto(1L, "Updated Name");
        Director updatedDirector = new Director(1L, "Updated Name");

        when(directorStorage.findById(1L)).thenReturn(Optional.of(director));
        when(directorStorage.updateDirector(any(Director.class))).thenReturn(updatedDirector);

        DirectorDto result = directorService.updateDirector(updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");

        verify(directorStorage).findById(1L);
        verify(directorStorage).updateDirector(any(Director.class));
    }

    @Test
    void updateDirector_NotFound_ShouldThrowException() {
        DirectorUpdateDto updateDto = new DirectorUpdateDto(999L, "Updated Name");

        when(directorStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> directorService.updateDirector(updateDto));

        verify(directorStorage).findById(999L);
        verify(directorStorage, never()).updateDirector(any());
    }

    @Test
    void deleteDirectorById_ShouldCallStorage() {
        doNothing().when(directorStorage).deleteDirector(1L);

        directorService.deleteDirectorById(1L);

        verify(directorStorage).deleteDirector(1L);
    }
}