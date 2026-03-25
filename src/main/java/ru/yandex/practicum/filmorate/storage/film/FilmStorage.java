package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> findById(Long id);

    List<Film> getFilms();

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film addLike(Long filmId, Long userId);

    Film deleteLike(Long filmId, Long userId);

    List<Film> getMostLikedFilms(int count, Long genreId, Integer year);

    default List<Film> getMostLikedFilms(int count) {
        return getMostLikedFilms(count, null, null);
    }

    default List<Film> getMostLikedFilms(int count, Long genreId) {
        return getMostLikedFilms(count, genreId, null);
    }

    List<Film> getFilmsByDirectorId(Long directorId, String sortParam);

    void deleteFilm(Long filmId);

    List<Film> getRecommendationsByUserId(Long id);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> searchFilms(String substring, String queryParam);
}
