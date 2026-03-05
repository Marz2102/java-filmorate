package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.Exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final InMemoryUserStorage userStorage;
    private final InMemoryFilmStorage filmStorage;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmService(final InMemoryUserStorage userStorage, InMemoryFilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addFilm(final Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Проблема с полем 'Дата релиза'");
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }

        return filmStorage.addFilm(film);
    }

    public Film updateFilm(final Film film) {
        validateRequestBody(film);
        log.info("Валидация запроса прошла успешно");

        checkToFindFilmById(film.getId());
        Film oldFilm = filmStorage.findById(film.getId()).get();

        if (film.getDescription() != null) {
            oldFilm.setDescription(film.getDescription());
            log.debug("Обновили описание фильма - {}", oldFilm.getDescription());
        }

        if (film.getName() != null) {
            oldFilm.setName(film.getName());
            log.debug("Обновили название фильма - {}", oldFilm.getName());
        }

        if (film.getReleaseDate() != null) {
            oldFilm.setReleaseDate(film.getReleaseDate());
            log.debug("Обновили дату релиза фильма - {}", oldFilm.getReleaseDate());
        }

        if (film.getDuration() > 0) {
            oldFilm.setDuration(film.getDuration());
            log.debug("Обновили продолжительность фильма - {}", oldFilm.getDuration());
        }

        return filmStorage.updateFilm(oldFilm);
    }

    public Film addLike(Long filmId, Long userId) {
        checkToFindFilmById(filmId);
        checkToFindUserById(userId);

        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(Long filmId, Long userId) {
        checkToFindFilmById(filmId);
        checkToFindUserById(userId);

        return filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getMostLikedFilms(int count) {
        return filmStorage.getMostLikedFilms(count);
    }

    private void checkToFindFilmById(Long id) {
        if (filmStorage.findById(id).isEmpty()) {
            log.info("Не найдено фильма с указанным id - {}", id);
            throw new NotFoundException("Фильм с id - " + id + " не найден");
        }
    }

    private void checkToFindUserById(Long id) {
        if (userStorage.findById(id).isEmpty()) {
            log.info("Не найдено пользователя с указанным id - {}", id);
            throw new NotFoundException("Пользователь с id - " + id + " не найден");
        }
    }

    private void validateRequestBody(Film film) {
        if (film.getId() == null) {
            log.warn("Отсутствует id");
            throw new ValidationException("Отсутствует id");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Проблема с полем 'Дата релиза'");
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }
    }
}
