package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository("FilmDao")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper filmRowMapper) {
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
    }
    @Override
    public Optional<Film> findById(Long id) {
        String query = "SELECT id, name, description, release_date, duration FROM films WHERE id = ?";
        try {
            Film film = jdbc.queryForObject(query, filmRowMapper, id);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getFilms() {
        String query = "SELECT id, name, description, release_date, duration FROM films";
        return jdbc.query(query, filmRowMapper);
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

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            film.setId(id);
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
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
            throw new RuntimeException("Не удалось обновить данные");
        }
        return film;
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        String query = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(query, filmId, userId);

        Film film = findById(filmId).get();
        film.addLike(userId);
        return film;
    }

    @Override
    public Film deleteLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);

        Film film = findById(filmId).get();
        film.deleteLike(userId);
        return film;
    }

    @Override
    public List<Film> getMostLikedFilms(int count) {
        String query = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration
                FROM films as f
                INNER JOIN (SELECT film_id, count(*) as cnt_likes
                            FROM likes
                            GROUP BY 1) as t
                ON f.id = t.film_id
                ORDER BY t.cnt_likes DESC
                LIMIT ?
               """;
        return jdbc.query(query, filmRowMapper, count);
    }
}
