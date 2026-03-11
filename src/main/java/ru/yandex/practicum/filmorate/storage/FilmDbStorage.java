package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository("FilmDao")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final RatingRowMapper ratingRowMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper filmRowMapper,
                         GenreRowMapper genreRowMapper, RatingRowMapper ratingRowMapper) {
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
        this.ratingRowMapper = ratingRowMapper;
    }
    @Override
    public Optional<Film> findById(Long id) {
        String query = "SELECT id, name, description, release_date, duration FROM films WHERE id = ?";

        try {
            Film film = jdbc.queryForObject(query, filmRowMapper, id);
            if (film != null) {
                film.setGenres(new HashSet<>(getGenresForFilm(id)));

                Optional<Rating> rating = getRatingForFilm(film.getId());
                rating.ifPresent(film::setMpa);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT id, name, description, release_date, duration FROM films ORDER BY id";

        List<Film> films = jdbc.query(query, filmRowMapper);
        for (Film film : films) {
            film.setGenres(new HashSet<>(getGenresForFilm(film.getId())));

            Optional<Rating> rating = getRatingForFilm(film.getId());
            rating.ifPresent(film::setMpa);
        }
        return films;
    }

    @Override
    public Film addFilm(Film film) {
        String query = "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            return ps;
        }, keyHolder);

        Long id =(Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        if (id != null) {
            film.setId(id);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить данные");
        }

        String queryForGenres = "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?) ";
        String queryForRating = "MERGE INTO film_rating (film_id, rating_id) VALUES (?, ?) ";

        if (film.getGenres() != null) {
            film.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .forEach(genre_id -> jdbc.update(queryForGenres, id, genre_id));

            if (film.getMpa() != null) {
                jdbc.update(queryForRating, id, film.getMpa().getId());
            }
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String query = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";
        int rowsUpdated = jdbc.update(query,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        if (rowsUpdated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обновить данные");
        }

        film.setGenres(new HashSet<>(getGenresForFilm(film.getId())));

        Optional<Rating> rating = getRatingForFilm(film.getId());
        rating.ifPresent(film::setMpa);
        return film;
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        String query = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(query, filmId, userId);

        Film film = findById(filmId).get();
        film.addLike(userId);

        film.setGenres(new HashSet<>(getGenresForFilm(filmId)));

        Optional<Rating> rating = getRatingForFilm(filmId);
        rating.ifPresent(film::setMpa);

        return film;
    }

    @Override
    public Film deleteLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);

        Film film = findById(filmId).get();
        film.deleteLike(userId);

        film.setGenres(new HashSet<>(getGenresForFilm(filmId)));

        Optional<Rating> rating = getRatingForFilm(filmId);
        rating.ifPresent(film::setMpa);

        return film;
    }

    @Override
    public List<Film> getMostLikedFilms(int count) {
        String query = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration
                FROM films as f
                INNER JOIN (SELECT film_id, count(*) as cnt_likes
                            FROM likes
                            GROUP BY film_id) as t ON f.id = t.film_id
                ORDER BY t.cnt_likes DESC
                LIMIT ?
               """;
        List<Film> films = jdbc.query(query, filmRowMapper, count);
        for (Film film : films) {
            film.setGenres(new HashSet<>(getGenresForFilm(film.getId())));

            Optional<Rating> rating = getRatingForFilm(film.getId());
            rating.ifPresent(film::setMpa);
        }

        return films;
    }

    public List<Genre> getGenres() {
        String query = "SELECT id, name FROM genres ORDER BY id DESC";
        return jdbc.query(query, genreRowMapper);
    }

    public Optional<Genre> findGenreById(Long id) {
        String query = "SELECT id, name FROM genres WHERE id = ?";

        try {
            Genre genre = jdbc.queryForObject(query, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Rating> getRatings() {
        String query = "SELECT id, name FROM ratings ORDER BY id DESC";
        return jdbc.query(query, ratingRowMapper);
    }

    public Optional<Rating> findRatingById(Long id) {
        String query = "SELECT id, name FROM ratings WHERE id = ?";

        try {
            Rating rating = jdbc.queryForObject(query, ratingRowMapper, id);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private List<Genre> getGenresForFilm(Long id) {
        String query = """
                SELECT g.id, g.name
                FROM film_genres as fg
                INNER JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id DESC
                """;

        return jdbc.query(query, genreRowMapper, id);
    }

    private Optional<Rating> getRatingForFilm(Long id) {
        String query = """
                SELECT r.id, r.name
                FROM films as f
                LEFT JOIN film_rating as fr ON f.id = fr.film_id
                LEFT JOIN ratings as r ON fr.rating_id = r.id
                WHERE f.id = ?
                """;

        try {
            Rating rating = jdbc.queryForObject(query, ratingRowMapper, id);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
