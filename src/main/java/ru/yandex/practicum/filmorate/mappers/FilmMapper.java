package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.filmDto.FilmCreateDto;
import ru.yandex.practicum.filmorate.filmDto.FilmDto;
import ru.yandex.practicum.filmorate.filmDto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.genreDto.GenreDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Comparator;
import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapFilmCreateDtoToFilm(FilmCreateDto filmCreateDto) {
        Film film = new Film();
        film.setName(filmCreateDto.getName());
        film.setDescription(filmCreateDto.getDescription());
        film.setReleaseDate(filmCreateDto.getReleaseDate());
        film.setDuration(filmCreateDto.getDuration());

        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setReleaseDate(film.getReleaseDate());
        filmDto.setDuration(film.getDuration());

        List<GenreDto> genres = film.getGenres().stream()
                .map(GenreMapper::mapToGenreDto)
                .sorted(Comparator.comparingLong(GenreDto::getId))
                .toList();
        filmDto.setGenres(genres);

        if (film.getMpa() != null) {
            filmDto.setMpa(RatingMapper.mapToRatingDto(film.getMpa()));
        }

        return filmDto;
    }

    public static Film updateFilmField(FilmUpdateDto filmUpdateDto, Film film) {
        if (filmUpdateDto.getDescription() != null) {
            film.setDescription(filmUpdateDto.getDescription());
            log.debug("Обновили описание фильма - {}", film.getDescription());
        }

        if (filmUpdateDto.getName() != null) {
            film.setName(filmUpdateDto.getName());
            log.debug("Обновили название фильма - {}", film.getName());
        }

        if (filmUpdateDto.getReleaseDate() != null) {
            film.setReleaseDate(filmUpdateDto.getReleaseDate());
            log.debug("Обновили дату релиза фильма - {}", film.getReleaseDate());
        }

        if (filmUpdateDto.getDuration() != null) {
            film.setDuration(filmUpdateDto.getDuration());
            log.debug("Обновили продолжительность фильма - {}", film.getDuration());
        }

        return film;
    }
}