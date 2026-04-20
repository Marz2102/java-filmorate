package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> findById(Long id);

    List<Film> getFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film addMark(Long filmId, Long userId, Double mark);

    Film deleteMark(Long filmId, Long userId);

    List<Film> getMostRatedFilms(int count, Long genreId, Integer year);

    default List<Film> getMostRatedFilms(int count) {
        return getMostRatedFilms(count, null, null);
    }

    default List<Film> getMostRatedFilms(int count, Long genreId) {
        return getMostRatedFilms(count, genreId, null);
    }

    List<Film> getFilmsByDirectorId(Long directorId, String sortParam);

    void deleteFilm(Long filmId);

    List<Film> getRecommendationsByUserId(Long id);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> searchFilms(String substring, String queryParam);
}
