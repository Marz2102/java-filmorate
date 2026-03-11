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
import ru.yandex.practicum.filmorate.mappers.RatingMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.ratingDto.RatingDto;
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
    private final RatingStorage ratingStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("UserDao") final UserStorage userStorage, @Qualifier("FilmDao") final FilmStorage filmStorage,
                       @Qualifier("GenreDao") final GenreStorage genreStorage, @Qualifier("RatingDao") final RatingStorage ratingStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
    }

    public FilmDto getFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id - " + id + " не найден"));

        Set<Genre> genreIds = filmStorage.getGenresForFilmId(id);
        film.setGenres(genreIds);

        Optional<Rating> optionalRating = filmStorage.getRatingForFilmId(id);
        Rating rating = optionalRating.orElse(null);
        film.setMpa(rating);

        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        List<Film> films = filmStorage.getFilms();

        Map<Long, Set<Genre>> allGenres = filmStorage.getGenresForAllFilms();
        films.forEach(film -> film.setGenres(allGenres.get(film.getId())));

        Map<Long, Rating> allRatings = filmStorage.getRatingForAllFilms();
        films.forEach(film -> film.setMpa(allRatings.get(film.getId())));

        return films.stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toList());
    }

    public FilmDto addFilm(FilmCreateDto filmCreateDto) {
        checkReleaseDate(filmCreateDto.getReleaseDate());
        checkGenresId(filmCreateDto.getGenreIds());
        checkRatingId(filmCreateDto.getRatingId());
        log.info("Валидация запроса прошла успешно");

        Film film = FilmMapper.mapFilmCreateDtoToFilm(filmCreateDto);

        Set<Genre> genres = filmCreateDto.getGenreIds()
                .stream()
                .map(this::getGenreById)
                .map(GenreMapper::mapGenreDtoToGenre)
                .collect(Collectors.toSet());
        film.setGenres(genres);

        Rating rating = RatingMapper.mapRatingDtoToRating(getRatingById(filmCreateDto.getRatingId()));
        film.setMpa(rating);

        film = filmStorage.addFilm(film);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(FilmUpdateDto filmUpdateDto) {
        checkReleaseDate(filmUpdateDto.getReleaseDate());
        checkRatingId(filmUpdateDto.getRatingId());
        log.info("Валидация запроса прошла успешно");

        Film updatedFilm = filmStorage.findById(filmUpdateDto.getId())
                .map(film -> FilmMapper.updateFilmField(filmUpdateDto, film))
                .orElseThrow(() -> new NotFoundException("Фильм с id - " + filmUpdateDto.getId() + " не найден"));

        Rating rating = RatingMapper.mapRatingDtoToRating(getRatingById(filmUpdateDto.getRatingId()));
        updatedFilm.setMpa(rating);

        updatedFilm = filmStorage.updateFilm(updatedFilm);

        Set<Genre> genreIds = filmStorage.getGenresForFilmId(updatedFilm.getId());
        updatedFilm.setGenres(genreIds);

        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public FilmDto addLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);

        Film film = filmStorage.addLike(filmId, userId);

        Set<Genre> genreIds = filmStorage.getGenresForFilmId(filmId);
        film.setGenres(genreIds);

        Optional<Rating> optionalRating = filmStorage.getRatingForFilmId(filmId);
        Rating rating = optionalRating.orElse(null);
        film.setMpa(rating);

        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);

        Film film = filmStorage.deleteLike(filmId, userId);

        Set<Genre> genreIds = filmStorage.getGenresForFilmId(filmId);
        film.setGenres(genreIds);

        Optional<Rating> optionalRating = filmStorage.getRatingForFilmId(filmId);
        Rating rating = optionalRating.orElse(null);
        film.setMpa(rating);

        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getMostLikedFilms(int count) {
        if (count <= 0) {
            log.info("Параметр count = {}", count);
            throw new ValidationException("Укажите положительный параметр count");
        }

        List<Film> films = filmStorage.getMostLikedFilms(count);

        Map<Long, Set<Genre>> allGenres = filmStorage.getGenresForAllFilms();
        films.forEach(film -> film.setGenres(allGenres.get(film.getId())));

        Map<Long, Rating> allRatings = filmStorage.getRatingForAllFilms();
        films.forEach(film -> film.setMpa(allRatings.get(film.getId())));

        return films.stream().map(FilmMapper::mapToFilmDto).collect(Collectors.toList());
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

    public RatingDto getRatingById(Long id) {
        return ratingStorage.findById(id)
                .map(RatingMapper::mapToRatingDto)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id - " + id + " не найден"));
    }

    public List<RatingDto> getRatings() {
        return ratingStorage.getRatings().stream()
                .map(RatingMapper::mapToRatingDto)
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

    private void checkRatingId(Long ratingId) {
        if (ratingId == null) {
            return;
        }

        Set<Long> allValidIds = ratingStorage.getRatings()
                .stream()
                .map(Rating::getId)
                .collect(Collectors.toSet());

        if (!allValidIds.contains(ratingId)) {
            log.warn("Проблема с полем 'Рейтинг'");
            throw new NotFoundException("Рейтинга с таким id не существует -" + ratingId);
        }
    }
}
