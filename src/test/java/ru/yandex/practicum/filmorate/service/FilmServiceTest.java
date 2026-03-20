package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.film.*;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserService userService;

    @Mock
    private GenreService genreService;

    @Mock
    private MpaService mpaService;

    @Mock
    private DirectorService directorService;

    @InjectMocks
    private FilmService filmService;

    private Film film;
    private FilmCreateDto filmCreateDto;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1L);
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setGenres(new HashSet<>());
        film.setDirectors(new HashSet<>());
        film.setLikes(new HashSet<>());

        filmCreateDto = new FilmCreateDto(
                "Inception",
                "A mind-bending thriller",
                LocalDate.of(2010, 7, 16),
                148,
                null,
                null,
                null
        );
    }

    @Test
    void getFilmById_ShouldReturnFilm() {
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));

        FilmDto result = filmService.getFilmById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Inception");

        verify(filmStorage).findById(1L);
    }

    @Test
    void getFilmById_NotFound_ShouldThrowException() {
        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.getFilmById(999L));

        verify(filmStorage).findById(999L);
    }

    @Test
    void getFilms_ShouldReturnList() {
        when(filmStorage.getFilms()).thenReturn(List.of(film));

        List<FilmDto> result = filmService.getFilms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(filmStorage).getFilms();
    }

    @Test
    void addFilm_ShouldReturnSavedFilm() {
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        FilmDto result = filmService.addFilm(filmCreateDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Inception");

        verify(filmStorage).addFilm(any(Film.class));
    }

    @Test
    void addFilm_WithReleaseDateBeforeCinemaBirthday_ShouldThrowException() {
        FilmCreateDto invalidDto = new FilmCreateDto(
                "Old Film",
                "Description",
                LocalDate.of(1890, 1, 1),
                100,
                null,
                null,
                null
        );

        assertThrows(ValidationException.class, () -> filmService.addFilm(invalidDto));

        verify(filmStorage, never()).addFilm(any());
    }

    @Test
    void addFilm_WithGenres_ShouldSetGenres() {
        List<FilmGenreDto> genres = List.of(new FilmGenreDto(1L), new FilmGenreDto(2L));
        FilmCreateDto dtoWithGenres = new FilmCreateDto(
                "Inception",
                "Description",
                LocalDate.of(2010, 7, 16),
                148,
                genres,
                null,
                null
        );

        when(genreService.getGenreById(1L)).thenReturn(new GenreDto(1L, "Комедия"));
        when(genreService.getGenreById(2L)).thenReturn(new GenreDto(2L, "Драма"));
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        filmService.addFilm(dtoWithGenres);

        verify(genreService, times(2)).getGenreById(anyLong());
        verify(filmStorage).addFilm(any(Film.class));
    }

    @Test
    void addFilm_WithDirectors_ShouldSetDirectors() {
        List<FilmDirectorDto> directors = List.of(new FilmDirectorDto(1L));
        FilmCreateDto dtoWithDirectors = new FilmCreateDto(
                "Inception",
                "Description",
                LocalDate.of(2010, 7, 16),
                148,
                null,
                directors,
                null
        );

        when(directorService.getDirectorById(1L)).thenReturn(new DirectorDto(1L, "Nolan"));
        when(filmStorage.addFilm(any(Film.class))).thenReturn(film);

        filmService.addFilm(dtoWithDirectors);

        verify(directorService).getDirectorById(1L);
        verify(filmStorage).addFilm(any(Film.class));
    }

    @Test
    void updateFilm_ShouldReturnUpdatedFilm() {
        FilmUpdateDto updateDto = new FilmUpdateDto(
                1L,
                "Updated Film",
                "Updated description",
                LocalDate.of(2010, 7, 16),
                150,
                null
        );

        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated description");
        updatedFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        updatedFilm.setDuration(150);

        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(filmStorage.updateFilm(any(Film.class))).thenReturn(updatedFilm);

        FilmDto result = filmService.updateFilm(updateDto);

        assertThat(result.getName()).isEqualTo("Updated Film");
        assertThat(result.getDuration()).isEqualTo(150);

        verify(filmStorage).findById(1L);
        verify(filmStorage).updateFilm(any(Film.class));
    }

    @Test
    void updateFilm_NotFound_ShouldThrowException() {
        FilmUpdateDto updateDto = new FilmUpdateDto(
                999L,
                "Updated Film",
                "Description",
                LocalDate.of(2010, 7, 16),
                150,
                null
        );

        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.updateFilm(updateDto));

        verify(filmStorage).findById(999L);
        verify(filmStorage, never()).updateFilm(any());
    }

    @Test
    void updateFilm_WithReleaseDateBeforeCinemaBirthday_ShouldThrowException() {
        FilmUpdateDto invalidDto = new FilmUpdateDto(
                1L,
                "Film",
                "Description",
                LocalDate.of(1890, 1, 1),
                100,
                null
        );

        assertThrows(ValidationException.class, () -> filmService.updateFilm(invalidDto));

        verify(filmStorage, never()).updateFilm(any());
    }

    @Test
    void addLike_ShouldAddLike() {
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userService.getUserById(1L)).thenReturn(null);
        when(filmStorage.addLike(1L, 1L)).thenReturn(film);

        FilmDto result = filmService.addLike(1L, 1L);

        assertThat(result).isNotNull();

        verify(filmStorage).findById(1L);
        verify(userService).getUserById(1L);
        verify(filmStorage).addLike(1L, 1L);
    }

    @Test
    void deleteLike_ShouldDeleteLike() {
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userService.getUserById(1L)).thenReturn(null);
        when(filmStorage.deleteLike(1L, 1L)).thenReturn(film);

        FilmDto result = filmService.deleteLike(1L, 1L);

        assertThat(result).isNotNull();

        verify(filmStorage).findById(1L);
        verify(userService).getUserById(1L);
        verify(filmStorage).deleteLike(1L, 1L);
    }

    @Test
    void getMostLikedFilms_WithCount_ShouldReturnList() {
        when(filmStorage.getMostLikedFilms(5, null, null)).thenReturn(List.of(film));

        List<FilmDto> result = filmService.getMostLikedFilms(5, null, null);

        assertThat(result).hasSize(1);

        verify(filmStorage).getMostLikedFilms(5, null, null);
    }

    @Test
    void getMostLikedFilms_WithNegativeCount_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> filmService.getMostLikedFilms(-1, null, null));

        verify(filmStorage, never()).getMostLikedFilms(anyInt(), any(), any());
    }

    @Test
    void getMostLikedFilms_WithGenreAndYear_ShouldReturnList() {
        when(filmStorage.getMostLikedFilms(10, 1L, 2020)).thenReturn(List.of(film));

        List<FilmDto> result = filmService.getMostLikedFilms(10, 1L, 2020);

        assertThat(result).hasSize(1);

        verify(filmStorage).getMostLikedFilms(10, 1L, 2020);
    }

    @Test
    void getAllFilmsByDirectorId_SortByLikes_ShouldReturnList() {
        when(filmStorage.getFilmsByDirectorId(1L, "likes")).thenReturn(List.of(film));

        List<FilmDto> result = filmService.getAllFilmsByDirectorId(1L, "likes");

        assertThat(result).hasSize(1);

        verify(filmStorage).getFilmsByDirectorId(1L, "likes");
    }

    @Test
    void getAllFilmsByDirectorId_SortByYear_ShouldReturnList() {
        when(filmStorage.getFilmsByDirectorId(1L, "year")).thenReturn(List.of(film));

        List<FilmDto> result = filmService.getAllFilmsByDirectorId(1L, "year");

        assertThat(result).hasSize(1);

        verify(filmStorage).getFilmsByDirectorId(1L, "year");
    }

    @Test
    void getCommonFilms_ShouldReturnList() {
        when(filmStorage.getCommonFilms(1L, 2L)).thenReturn(List.of(film));
        when(userService.getUserById(1L)).thenReturn(null);
        when(userService.getUserById(2L)).thenReturn(null);

        List<FilmDto> result = filmService.getCommonFilms(1L, 2L);

        assertThat(result).hasSize(1);

        verify(userService).getUserById(1L);
        verify(userService).getUserById(2L);
        verify(filmStorage).getCommonFilms(1L, 2L);
    }

    @Test
    void deleteFilm_ShouldDeleteFilm() {
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        doNothing().when(filmStorage).deleteFilm(1L);

        filmService.deleteFilm(1L);

        verify(filmStorage).findById(1L);
        verify(filmStorage).deleteFilm(1L);
    }

    @Test
    void deleteFilm_NotFound_ShouldThrowException() {
        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> filmService.deleteFilm(999L));

        verify(filmStorage).findById(999L);
        verify(filmStorage, never()).deleteFilm(anyLong());
    }
}