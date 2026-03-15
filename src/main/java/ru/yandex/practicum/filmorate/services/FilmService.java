package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.Exceptions.ValidationException;
import ru.yandex.practicum.filmorate.filmDto.FilmCreateDto;
import ru.yandex.practicum.filmorate.filmDto.FilmDto;
import ru.yandex.practicum.filmorate.filmDto.FilmUpdateDto;
import ru.yandex.practicum.filmorate.genreDto.GenreDto;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.mpaDto.MpaDto;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("UserDao") final UserStorage userStorage, @Qualifier("FilmDao") final FilmStorage filmStorage,
                       @Qualifier("GenreDao") final GenreStorage genreStorage, @Qualifier("MpaDao") final MpaStorage mpaStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public FilmDto getFilmById(Long id) {
        return FilmMapper.mapToFilmDto(filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id - " + id + " не найден")));
    }

    public List<FilmDto> getFilms() {
        return filmStorage.getFilms()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto addFilm(FilmCreateDto filmCreateDto) {
        checkReleaseDate(filmCreateDto.getReleaseDate());
        checkGenresId(filmCreateDto.getGenreIds());
        checkMpaId(filmCreateDto.getMpaId());
        log.info("Валидация запроса прошла успешно");

        Film film = FilmMapper.mapFilmCreateDtoToFilm(filmCreateDto);

        Set<Genre> genres = filmCreateDto.getGenreIds()
                .stream()
                .map(this::getGenreById)
                .map(GenreMapper::mapGenreDtoToGenre)
                .collect(Collectors.toSet());
        film.setGenres(genres);

        if (filmCreateDto.getMpaId() != null) {
            Mpa mpa = MpaMapper.mapMpaDtoToMpa(getMpaById(filmCreateDto.getMpaId()));
            film.setMpa(mpa);
        }

        film = filmStorage.addFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(FilmUpdateDto filmUpdateDto) {
        checkReleaseDate(filmUpdateDto.getReleaseDate());
        log.info("Валидация запроса прошла успешно");

        Film updatedFilm = filmStorage.findById(filmUpdateDto.getId())
                .map(film -> FilmMapper.updateFilmField(filmUpdateDto, film))
                .orElseThrow(() -> new NotFoundException("Фильм с id - " + filmUpdateDto.getId() + " не найден"));

        return FilmMapper.mapToFilmDto(filmStorage.updateFilm(updatedFilm));
    }

    public FilmDto addLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);
        return FilmMapper.mapToFilmDto(filmStorage.addLike(filmId, userId));
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);
        return FilmMapper.mapToFilmDto(filmStorage.deleteLike(filmId, userId));
    }

    public List<FilmDto> getMostLikedFilms(int count) {
        if (count <= 0) {
            log.info("Параметр count = {}", count);
            throw new ValidationException("Укажите положительный параметр count");
        }

        return filmStorage.getMostLikedFilms(count)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public GenreDto getGenreById(Long id) {
        return genreStorage.findById(id)
                .map(GenreMapper::mapToGenreDto)
                .orElseThrow(() -> new NotFoundException("Жанр с id - " + id + " не найден"));
    }

    public List<GenreDto> getGenres() {
        return genreStorage.getGenres().stream()
                .map(GenreMapper::mapToGenreDto)
                .toList();
    }

    public MpaDto getMpaById(Long id) {
        return mpaStorage.findById(id)
                .map(MpaMapper::mapToMpaDto)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id - " + id + " не найден"));
    }

    public List<MpaDto> getAllMpa() {
        return mpaStorage.getAllMpa().stream()
                .map(MpaMapper::mapToMpaDto)
                .toList();
    }

    private void checkToFindByIds(Long filmId, Long userId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            log.info("Не найдено фильма с указанным id - {}", filmId);
            throw new NotFoundException("Фильм с id - " + filmId + " не найден");
        }

        if (userStorage.findById(userId).isEmpty()) {
            log.info("Не найдено пользователя с указанным id - {}", userId);
            throw new NotFoundException("Пользователь с id - " + userId + " не найден");
        }
    }

    private void checkReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null && releaseDate.isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Проблема с полем 'Дата релиза'");
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }
    }

    private void checkGenresId(Set<Long> genresIds) {
        if (genresIds == null || genresIds.isEmpty()) {
            return;
        }

        Set<Long> allValidIds = genreStorage.getGenres()
                .stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Long> notValidIds = genresIds.stream()
                .filter(id -> !allValidIds.contains(id))
                .collect(Collectors.toSet());

        if (!notValidIds.isEmpty()) {
            log.warn("Проблема с полем 'Жанры'");
            throw new NotFoundException("Жанров с такими id не существует -" + notValidIds);
        }
    }

    private void checkMpaId(Long id) {
        if (id == null) {
            return;
        }

        Set<Long> allValidIds = mpaStorage.getAllMpa()
                .stream()
                .map(Mpa::getId)
                .collect(Collectors.toSet());

        if (!allValidIds.contains(id)) {
            log.warn("Проблема с полем 'Рейтинг'");
            throw new NotFoundException("Рейтинга с таким id не существует -" + id);
        }
    }
}
