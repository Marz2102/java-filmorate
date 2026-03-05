package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FilmStorage {
    final Map<Long, Film> films = new HashMap<>();

    Optional<Film> findById(Long id);
    List<Film> getFilms();
    Film addFilm(Film film);
    Film updateFilm(Film film);
    Film addLike(Long filmId, Long userId);
    Film deleteLike(Long filmId, Long userId);
    List<Film> getMostLikedFilms(int count);
    List<Film> clear();
    Long generateNextId();
}
