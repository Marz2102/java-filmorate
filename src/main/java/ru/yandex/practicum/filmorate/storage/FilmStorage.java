package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public interface FilmStorage {
    Optional<Film> findById(Long id);

    List<Film> getFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film addLike(Long filmId, Long userId);

    Film deleteLike(Long filmId, Long userId);

    List<Film> getMostLikedFilms(int count);

    //Добавил дефолтные определения, чтобы не сломалась старая реализация InMemoryFilmStorage
    //В FilmDbStorage методы переопределяются
    default Optional<Genre> findGenreById(Long id) {
        return Optional.empty();
    }

    default List<Genre> getGenres() {
        return List.of();
    }

    default Optional<Rating> findRatingById(Long id) {
        return Optional.empty();
    }

    default List<Rating> getRatings() {
        return List.of();
    }

    //Добавил дефолтное определение метода, чтобы не имплементировать его в FilmDbStorage
    //(в принципе он вообще не нужен в новой реализации)
    default Long generateNextId() {
        Random random = new Random();
        return random.nextLong();
    };
}
