package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;
import java.util.stream.Collectors;

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

    public static FilmDto mapFilmToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();

        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setReleaseDate(film.getReleaseDate());
        filmDto.setDuration(film.getDuration());

        List<GenreDto> genres = film.getGenres().stream()
                .map(GenreMapper::mapGenreToGenreDto)
                .sorted(Comparator.comparingLong(GenreDto::getId))
                .toList();
        filmDto.setGenres(genres);

        List<DirectorDto> directors = film.getDirectors().stream()
                .map(DirectorMapper::mapDirectorToDirectorDto)
                .sorted(Comparator.comparingLong(DirectorDto::getId))
                .toList();
        filmDto.setDirectors(directors);

        if (film.getMpa() != null) {
            filmDto.setMpa(MpaMapper.mapMpaToMpaDto(film.getMpa()));
        }

        filmDto.setLikes(new ArrayList<>(film.getLikes()));
        filmDto.setRate(film.getRate());

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

        if (filmUpdateDto.getMpa() != null) {
            Mpa mpa = new Mpa();
            mpa.setId(filmUpdateDto.getMpa().getId());
            film.setMpa(mpa);
            log.debug("Обновили рейтинг фильма, id={}", film.getMpa().getId());
        }

        if (filmUpdateDto.getGenres() != null) {
            if (filmUpdateDto.getGenres().isEmpty()) {
                film.setGenres(new HashSet<>());
                log.debug("Очистили список жанров фильма");
            } else {
                Set<Genre> genres = filmUpdateDto.getGenres().stream()
                        .map(d -> {
                            Genre genre = new Genre();
                            genre.setId(d.getId());
                            return genre;
                        })
                        .collect(Collectors.toSet());
                film.setGenres(genres);
                log.debug("Обновили список жанров фильма, ids={}", film.getGenres());
            }
        }

        if (filmUpdateDto.getDirectors() == null || filmUpdateDto.getDirectors().isEmpty()) {
            film.setDirectors(new HashSet<>());
            log.debug("Очистили список режиссёров фильма");
        } else {
            Set<Director> directors = filmUpdateDto.getDirectors().stream()
                    .map(d -> {
                        Director director = new Director();
                        director.setId(d.getId());
                        return director;
                    })
                    .collect(Collectors.toSet());
            film.setDirectors(directors);
            log.debug("Обновили режиссёров фильма, ids={}", film.getDirectors());
        }

        return film;
    }
}
