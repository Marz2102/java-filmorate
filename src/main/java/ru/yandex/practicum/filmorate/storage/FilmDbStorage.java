package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Transactional
@Repository("FilmDao")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;

    public FilmDbStorage(DataSource dataSource, FilmRowMapper filmRowMapper,
                         GenreRowMapper genreRowMapper, MpaRowMapper mpaRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String query = """
               SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id as mpa_id, r.name as mpa_name
               FROM films as f
               LEFT JOIN film_rating as fr ON f.id = fr.film_id
               LEFT JOIN ratings as r ON r.id = fr.rating_id
               WHERE f.id = ?
               """;

        try {
            Film film = jdbc.queryForObject(query, filmRowMapper, id);

            if (film != null) {
                film.setGenres(getGenresForFilmId(id));
            }

            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getFilms() {
        String query = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id as mpa_id, r.name as mpa_name
                FROM films as f
                LEFT JOIN film_rating as fr ON f.id = fr.film_id
                LEFT JOIN ratings as r ON r.id = fr.rating_id
                ORDER BY f.id
                """;

        List<Film> films = jdbc.query(query, filmRowMapper);

        Map<Long, Set<Genre>> allGenres = getGenresForAllFilms();
        films.forEach(film -> film.setGenres(allGenres.getOrDefault(film.getId(), Collections.emptySet())));

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

        Long id = (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        if (id != null) {
            film.setId(id);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить данные");
        }

        String queryForGenres = "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?) ";
        String queryForMpa = "MERGE INTO film_rating (film_id, rating_id) VALUES (?, ?) ";

        if (film.getGenres() != null) {
            film.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .forEach(genre_id -> jdbc.update(queryForGenres, id, genre_id));
        }

        if (film.getMpa() != null) {
            jdbc.update(queryForMpa, id, film.getMpa().getId());
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

        Set<Genre> genres = getGenresForFilmId(film.getId());
        film.setGenres(genres);

        Mpa mpa = getMpaForFilmId(film.getId()).orElse(null);
        film.setMpa(mpa);

        return film;
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        String query = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(query, filmId, userId);

        Film film = findById(filmId).get();
        film.addLike(userId);

        film.setGenres(getGenresForFilmId(filmId));

        return film;
    }

    @Override
    public Film deleteLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);

        Film film = findById(filmId).get();
        film.deleteLike(userId);

        film.setGenres(getGenresForFilmId(filmId));

        return film;
    }

    @Override
    public List<Film> getMostLikedFilms(int count) {
        String query = """
               SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id as mpa_id, r.name as mpa_name
               FROM films as f
               LEFT JOIN (SELECT film_id, count(*) as cnt_likes
                           FROM likes
                           GROUP BY film_id) as t ON f.id = t.film_id
               LEFT JOIN film_rating as fr ON f.id = fr.film_id
               LEFT JOIN ratings as r ON r.id = fr.rating_id
               ORDER BY t.cnt_likes DESC
               LIMIT ?
               """;

        List<Film> films = jdbc.query(query, filmRowMapper, count);

        Map<Long, Set<Genre>> allGenres = getGenresForAllFilms();
        films.forEach(film -> film.setGenres(allGenres.getOrDefault(film.getId(), Collections.emptySet())));

        return films;
    }

    private Set<Genre> getGenresForFilmId(Long id) {
        String query = """
                SELECT g.id, g.name
                FROM film_genres as fg
                INNER JOIN genres as g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                """;
        return new HashSet<>(jdbc.query(query, genreRowMapper, id));
    }

    private Map<Long, Set<Genre>> getGenresForAllFilms() {
        String query = """
                SELECT fg.film_id, g.id as genre_id, g.name
                FROM film_genres as fg
                INNER JOIN genres as g ON fg.genre_id = g.id
                """;
        Map<Long, Set<Genre>> filmsGenres = new HashMap<>();

        jdbc.query(query, (rs) -> {
           Long filmId = rs.getLong("film_id");
           Set<Genre> genres = filmsGenres.computeIfAbsent(filmId, k -> new HashSet<>());

           Genre genre = new Genre();
           genre.setId(rs.getLong("genre_id"));
           genre.setName(rs.getString("name"));

           genres.add(genre);
        });

        return filmsGenres;
    }

    private Optional<Mpa> getMpaForFilmId(Long id) {
        String query = """
                SELECT r.id, r.name
                FROM film_rating as fr
                INNER JOIN ratings as r ON r.id = fr.rating_id
                WHERE fr.film_id = ?
                """;

        try {
            Mpa mpa = jdbc.queryForObject(query, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
