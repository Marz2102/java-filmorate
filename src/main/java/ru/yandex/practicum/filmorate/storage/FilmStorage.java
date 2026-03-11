package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    Optional<Film> findById(Long id);

    List<Film> getFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film addLike(Long filmId, Long userId);

    Film deleteLike(Long filmId, Long userId);

    List<Film> getMostLikedFilms(int count);

    Set<Genre> getGenresForFilmId(Long id);

    Optional<Rating> getRatingForFilmId(Long id);

    Map<Long, Set<Genre>> getGenresForAllFilms();

    Map<Long, Rating> getRatingForAllFilms();
}
