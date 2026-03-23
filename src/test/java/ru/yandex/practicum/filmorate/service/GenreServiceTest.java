package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreStorage genreStorage;

    @InjectMocks
    private GenreService genreService;

    private Genre genre;
    private GenreDto genreDto;

    @BeforeEach
    void setUp() {
        genre = new Genre(1L, "Комедия");
        genreDto = new GenreDto(1L, "Комедия");
    }

    @Test
    void getGenreById_ShouldReturnGenre() {
        when(genreStorage.findById(1L)).thenReturn(Optional.of(genre));

        GenreDto result = genreService.getGenreById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Комедия");

        verify(genreStorage).findById(1L);
    }

    @Test
    void getGenreById_NotFound_ShouldThrowException() {
        when(genreStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> genreService.getGenreById(999L));

        verify(genreStorage).findById(999L);
    }

    @Test
    void getGenres_ShouldReturnList() {
        List<Genre> genres = List.of(
                new Genre(1L, "Комедия"),
                new Genre(2L, "Драма")
        );
        when(genreStorage.getGenres()).thenReturn(genres);

        List<GenreDto> result = genreService.getGenres();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Комедия");
        assertThat(result.get(1).getName()).isEqualTo("Драма");

        verify(genreStorage).getGenres();
    }
}
