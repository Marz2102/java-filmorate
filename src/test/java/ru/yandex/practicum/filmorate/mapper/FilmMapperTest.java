package ru.yandex.practicum.filmorate.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FilmMapperTest {

    @Test
    void mapFilmCreateDtoToFilm_ShouldMapCorrectly() {
        FilmCreateDto dto = new FilmCreateDto(
                "Inception",
                "Description",
                LocalDate.of(2010, 7, 16),
                148,
                null,
                null,
                null
        );

        Film film = FilmMapper.mapFilmCreateDtoToFilm(dto);

        assertThat(film.getName()).isEqualTo("Inception");
        assertThat(film.getDescription()).isEqualTo("Description");
        assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));
        assertThat(film.getDuration()).isEqualTo(148);
    }

    @Test
    void updateFilmField_ShouldUpdateNonNullFields() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Old Name");
        film.setDescription("Old Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        FilmUpdateDto updateDto = new FilmUpdateDto(
                1L,
                "New Name",
                "New Description",
                LocalDate.of(2010, 7, 16),
                150,
                null
        );

        Film updated = FilmMapper.updateFilmField(updateDto, film);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New Description");
        assertThat(updated.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));
        assertThat(updated.getDuration()).isEqualTo(150);
    }

    @Test
    void updateFilmField_WithNullFields_ShouldKeepOldValues() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Old Name");
        film.setDescription("Old Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        FilmUpdateDto updateDto = new FilmUpdateDto(
                1L,
                null,
                null,
                null,
                null,
                null
        );

        Film updated = FilmMapper.updateFilmField(updateDto, film);

        assertThat(updated.getName()).isEqualTo("Old Name");
        assertThat(updated.getDescription()).isEqualTo("Old Description");
        assertThat(updated.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(updated.getDuration()).isEqualTo(100);
    }

    @Test
    void updateFilmField_WithEmptyDirectors_ShouldClearDirectors() {
        Film film = new Film();
        film.setDirectors(Set.of(new Director(1L, "Nolan")));

        FilmUpdateDto updateDto = new FilmUpdateDto(
                1L,
                null,
                null,
                null,
                null,
                java.util.List.of()
        );

        Film updated = FilmMapper.updateFilmField(updateDto, film);

        assertThat(updated.getDirectors()).isEmpty();
    }

    @Test
    void updateFilmField_WithDirectors_ShouldSetDirectors() {
        Film film = new Film();
        film.setDirectors(new HashSet<>());

        FilmUpdateDto updateDto = new FilmUpdateDto(
                1L,
                null,
                null,
                null,
                null,
                java.util.List.of(
                        new ru.yandex.practicum.filmorate.dto.director.UpdateDirectorDto(1L),
                        new ru.yandex.practicum.filmorate.dto.director.UpdateDirectorDto(2L)
                )
        );

        Film updated = FilmMapper.updateFilmField(updateDto, film);

        assertThat(updated.getDirectors()).hasSize(2);
        assertThat(updated.getDirectors()).extracting(Director::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }
}