package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FilmStorage {
    final Map<Long, Film> films = new HashMap<>();

    List<Film> getFilms();
    Film addFilm(Film film);
    Film updateFilm(Film film);
    Long generateNextId();
}
