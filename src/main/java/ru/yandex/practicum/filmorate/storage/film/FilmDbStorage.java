package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

@Transactional
@Repository("FilmDao")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;
    private final MpaRowMapper mpaRowMapper;

    public FilmDbStorage(DataSource dataSource, FilmRowMapper filmRowMapper, GenreRowMapper genreRowMapper,
                         DirectorRowMapper directorRowMapper, MpaRowMapper mpaRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
        this.directorRowMapper = directorRowMapper;
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String query = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id as mpa_id, r.name as mpa_name
                FROM films as f
                LEFT JOIN ratings as r ON f.rating_id = r.id
                WHERE f.id = ?
                """;

        try {
            Film film = jdbc.queryForObject(query, filmRowMapper, id);

            if (film != null) {
                film.setGenres(getGenresForFilmId(id));
                film.setRate(getRateForFilmId(id));
                film.setDirectors(getDirectorsForFilmId(id));
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
                LEFT JOIN ratings as r ON f.rating_id = r.id
                ORDER BY f.id
                """;

        List<Film> films = jdbc.query(query, filmRowMapper);

        setAllGenresDirectorsAndMarks(films);

        return films;
    }

    @Override
    public Film addFilm(Film film) {
        String query = "INSERT INTO films (name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());

            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            return ps;
        }, keyHolder);

        Long id = (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        if (id != null) {
            film.setId(id);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить данные");
        }

        String queryForGenres = "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?) ";
        String queryForDirectors = "MERGE INTO film_directors (film_id, director_id) VALUES (?, ?)";

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            jdbc.batchUpdate(
                    queryForGenres,
                    film.getGenres(),
                    film.getGenres().size(),
                    (ps, genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genre.getId());
                    }
            );
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            jdbc.batchUpdate(
                    queryForDirectors,
                    film.getDirectors(),
                    film.getDirectors().size(),
                    (ps, director) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, director.getId());
                    }
            );
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String updateFilmSql =
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE id = ?";

        int rowsUpdated = jdbc.update(updateFilmSql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        if (rowsUpdated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обновить данные");
        }

        String deleteDirectorsSql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbc.update(deleteDirectorsSql, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            String insertDirectorsSql =
                    "MERGE INTO film_directors (film_id, director_id) VALUES (?, ?)";

            jdbc.batchUpdate(
                    insertDirectorsSql,
                    film.getDirectors(),
                    film.getDirectors().size(),
                    (ps, director) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, director.getId());
                    }
            );
        }

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteGenresSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertGenresSql =
                    "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            jdbc.batchUpdate(
                    insertGenresSql,
                    film.getGenres(),
                    film.getGenres().size(),
                    (ps, genre) -> {
                        ps.setLong(1, film.getId());
                        ps.setLong(2, genre.getId());
                    }
            );
        }

        film.setDirectors(getDirectorsForFilmId(film.getId()));
        film.setGenres(getGenresForFilmId(film.getId()));
        film.setRate(getRateForFilmId(film.getId()));
        film.setMpa(getMpaForFilmId(film.getId()).orElse(null));

        return film;
    }

    @Override
    public Film addMark(Long filmId, Long userId, Double mark) {
        String query = "MERGE INTO marks (film_id, user_id, mark) VALUES (?, ?, ?)";
        jdbc.update(query, filmId, userId, mark);

        return findById(filmId).orElse(null);
    }

    @Override
    public Film deleteMark(Long filmId, Long userId) {
        String query = "DELETE FROM marks WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);

        return findById(filmId).orElse(null);
    }

    @Override
    public List<Film> getMostRatedFilms(int count, Long genreId, Integer year) {
        StringBuilder query = new StringBuilder("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   r.id as mpa_id, r.name as mpa_name,
                   AVG(m.mark) as average_mark
            FROM films as f
            LEFT JOIN marks as m ON f.id = m.film_id
            LEFT JOIN ratings as r ON f.rating_id = r.id
            """);

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            query.append(" INNER JOIN film_genres as fg ON f.id = fg.film_id AND fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            query.append(" WHERE YEAR(f.release_date) = ?");
            params.add(year);
        }

        query.append("""
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration,
                     r.id, r.name
            ORDER BY average_mark DESC
            LIMIT ?
            """);
        params.add(count);

        List<Film> films = jdbc.query(query.toString(), filmRowMapper, params.toArray());

        setAllGenresDirectorsAndMarks(films);

        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorId(Long directorId, String sortParam) {
        String query;
        if ("rate".equals(sortParam)) {
            query = """
                    SELECT f.id,
                           f.name,
                           f.description,
                           f.release_date,
                           f.duration,
                           r.id   AS mpa_id,
                           r.name AS mpa_name
                    FROM films AS f
                    LEFT JOIN ratings AS r ON r.id = f.rating_id
                    JOIN film_directors AS fd ON fd.film_id = f.id
                    JOIN directors AS d ON d.id = fd.director_id
                    LEFT JOIN marks m ON m.film_id = f.id
                    WHERE d.id = ?
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, r.id, r.name
                    ORDER BY AVG(m.mark) DESC
                    """;
        } else if ("year".equals(sortParam)) {
            query = """
                    SELECT f.id,
                           f.name,
                           f.description,
                           f.release_date,
                           f.duration,
                           r.id   AS mpa_id,
                           r.name AS mpa_name
                    FROM films AS f
                    LEFT JOIN ratings AS r ON r.id = f.rating_id
                    JOIN film_directors AS fd ON fd.film_id = f.id
                    JOIN directors AS d ON d.id = fd.director_id
                    WHERE d.id = ?
                    ORDER BY f.release_date
                    """;
        } else {
            throw new IllegalArgumentException("Unknown sort param: " + sortParam);
        }

        List<Film> films = jdbc.query(query, filmRowMapper, directorId);

        setAllGenresDirectorsAndMarks(films);

        return films;
    }

    @Override
    public void deleteFilm(Long filmId) {
        String query = "DELETE FROM films WHERE id = ?";
        int rowsDeleted = jdbc.update(query, filmId);

        if (rowsDeleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм с id " + filmId + " не найден");
        }
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String query = """
               SELECT f.id, f.name, f.description, f.release_date, f.duration, r.id as mpa_id, r.name as mpa_name
               FROM films as f
               INNER JOIN marks as m1 ON m1.film_id = f.id AND m1.user_id = ?
               INNER JOIN marks as m2 ON m2.film_id = f.id AND m2.user_id = ? AND m1.mark = m2.mark
               INNER JOIN (SELECT film_id, AVG(mark) as mark
                           FROM marks
                           GROUP BY film_id) as t ON f.id = t.film_id
               LEFT JOIN ratings as r ON f.rating_id = r.id
               ORDER BY t.mark DESC
               """;

        List<Film> films = jdbc.query(query, filmRowMapper, userId, friendId);

        setAllGenresDirectorsAndMarks(films);

        return films;
    }

    @Override
    public List<Film> getRecommendationsByUserId(Long id) {

        String similarUsersQuery = """
                SELECT m2.user_id,
                    COUNT(*) AS common_marks
                FROM marks AS m1
                JOIN marks AS m2 ON m1.film_id = m2.film_id
                WHERE m1.user_id = ?
                    AND m2.user_id != ?
                GROUP BY m2.user_id
                ORDER BY common_marks DESC
                """;
        String recommendFilmsQuery = """
                SELECT film_id
                FROM marks
                WHERE user_id = ?
                AND film_id NOT IN (
                    SELECT film_id FROM marks WHERE user_id = ?
                )
                """;

        List<Map<String, Object>> similarUsers = jdbc.queryForList(similarUsersQuery, id, id);

        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> recommendedFilmIds = new LinkedHashSet<>();

        similarUsers
                .stream()
                .map(user -> (Long) user.get("user_id"))
                .map(userId -> jdbc.queryForList(recommendFilmsQuery, Long.class, userId, id))
                .forEach(recommendedFilmIds::addAll);

        if (recommendedFilmIds.isEmpty()) {
            return Collections.emptyList();
        }

        return recommendedFilmIds
                .stream()
                .map(filmId -> findById(filmId)
                        .orElseThrow(() -> new NotFoundException("Фильм с id - " + filmId + " не найден")))
                .toList();
    }

    @Override
    public List<Film> searchFilms(String substring, String queryParam) {
        if (substring == null || substring.isBlank()) {
            return Collections.emptyList();
        }

        StringBuilder query = new StringBuilder("""
        SELECT f.id, f.name, f.description, f.release_date, f.duration,
               r.id as mpa_id, r.name as mpa_name
        FROM films as f
        LEFT JOIN marks as m ON f.id = m.film_id
        LEFT JOIN ratings as r ON f.rating_id = r.id
        LEFT JOIN film_directors as fd ON fd.film_id = f.id
        LEFT JOIN directors as d ON d.id = fd.director_id
        WHERE
        """);

        List<String> params = new ArrayList<>();

        if (queryParam.contains("director")) {
            query.append(" d.name ILIKE ? ");
            params.add('%' + substring + '%');
            if (queryParam.contains("title")) {
                query.append(" OR f.name ILIKE ? ");
                params.add('%' + substring + '%');
            }
        } else {
            query.append(" f.name ILIKE ? ");
            params.add('%' + substring + '%');
        }

        query.append("""
        GROUP BY f.id, f.name, f.description, f.release_date, f.duration,
                 r.id, r.name
        ORDER BY AVG(m.mark) DESC
        """);

        List<Film> films = jdbc.query(query.toString(), filmRowMapper, params.toArray());

        setAllGenresDirectorsAndMarks(films);

        return films;
    }

    private void setAllGenresDirectorsAndMarks(List<Film> films) {
        Map<Long, Set<Genre>> allGenres = getGenresForAllFilms();
        films.forEach(film -> film.setGenres(allGenres.getOrDefault(film.getId(), Collections.emptySet())));

        Map<Long, Set<Director>> allDirectors = getDirectorsForAllFilms();
        films.forEach(film -> film.setDirectors(allDirectors.getOrDefault(film.getId(), Collections.emptySet())));

        Map<Long, Double> allRates = getRateForAllFilms();
        films.forEach(film -> film.setRate(allRates.getOrDefault(film.getId(), 0.0)));
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

    private Set<Director> getDirectorsForFilmId(Long id) {
        String query = """
                SELECT d.id, d.name
                FROM film_directors as fd
                INNER JOIN directors as d ON fd.director_id = d.id
                WHERE fd.film_id = ?
                """;
        return new HashSet<>(jdbc.query(query, directorRowMapper, id));
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

    private Map<Long, Set<Director>> getDirectorsForAllFilms() {
        String query = """
                SELECT fd.film_id, d.id as director_id, d.name
                FROM film_directors as fd
                INNER JOIN directors as d ON fd.director_id = d.id
                """;
        Map<Long, Set<Director>> filmsDirectors = new HashMap<>();

        jdbc.query(query, (rs) -> {
            Long filmId = rs.getLong("film_id");
            Set<Director> directors = filmsDirectors.computeIfAbsent(filmId, k -> new HashSet<>());

            Director director = new Director();
            director.setId(rs.getLong("director_id"));
            director.setName(rs.getString("name"));

            directors.add(director);
        });

        return filmsDirectors;
    }

    private Optional<Mpa> getMpaForFilmId(Long id) {
        String query = """
                SELECT r.id, r.name
                FROM films as f
                INNER JOIN ratings as r ON f.rating_id = r.id
                WHERE f.id = ?
                """;

        try {
            Mpa mpa = jdbc.queryForObject(query, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Double getRateForFilmId(Long id) {
        String query = """
                SELECT AVG(m.mark) as rate
                FROM films as f
                INNER JOIN marks as m ON f.id = m.film_id
                WHERE f.id = ?
                """;

        return jdbc.queryForObject(query, Double.class, id);
    }

    private Map<Long, Double> getRateForAllFilms() {
        String query = """
                SELECT f.id as film_id, AVG(m.mark) as rate
                FROM films as f
                INNER JOIN marks as m ON f.id = m.film_id
                GROUP BY f.id
                """;

        Map<Long, Double> allRates = new HashMap<>();

        jdbc.query(query, (rs) -> {
            Long filmId = rs.getLong("film_id");
            Double mark = rs.getDouble("rate");

            allRates.put(filmId, mark);
        });

        return allRates;
    }
}