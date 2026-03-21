package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.user.LikesDto;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
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

    public FilmDbStorage(DataSource dataSource, FilmRowMapper filmRowMapper,
                         GenreRowMapper genreRowMapper, DirectorRowMapper directorRowMapper, MpaRowMapper mpaRowMapper) {
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
                film.setLikes(getLikesForFilmId(id));
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

        setAllGenresDirectorsAndLikes(films);

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
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";

        int rowsUpdated = jdbc.update(updateFilmSql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
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

        film.setDirectors(getDirectorsForFilmId(film.getId()));
        film.setGenres(getGenresForFilmId(film.getId()));
        film.setLikes(getLikesForFilmId(film.getId()));
        film.setMpa(getMpaForFilmId(film.getId()).orElse(null));

        return film;
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        String query = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbc.update(query, filmId, userId);

        return findById(filmId).orElse(null);
    }

    @Override
    public Film deleteLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(query, filmId, userId);

        return findById(filmId).orElse(null);
    }

    @Override
    public List<Film> getMostLikedFilms(int count, Long genreId, Integer year) {
        StringBuilder query = new StringBuilder("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   r.id as mpa_id, r.name as mpa_name,
                   COUNT(l.user_id) as likes_count
            FROM films as f
            LEFT JOIN likes as l ON f.id = l.film_id
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
            ORDER BY likes_count DESC
            LIMIT ?
            """);
        params.add(count);

        List<Film> films = jdbc.query(query.toString(), filmRowMapper, params.toArray());

        setAllGenresDirectorsAndLikes(films);

        return films;
    }

    @Override
    public List<Film> getFilmsByDirectorId(Long directorId, String sortParam) {
        String query;
        if ("likes".equals(sortParam)) {
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
                    LEFT JOIN likes l ON l.film_id = f.id
                    WHERE d.id = ?
                    GROUP BY f.id, f.name, f.description, f.release_date, f.duration, r.id, r.name
                    ORDER BY COUNT(l.user_id) DESC
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

        setAllGenresDirectorsAndLikes(films);

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
               INNER JOIN likes as l1 ON l1.film_id = f.id AND l1.user_id = ?
               INNER JOIN likes as l2 ON l2.film_id = f.id AND l2.user_id = ?
               INNER JOIN (SELECT film_id, count(*) as cnt_likes
                           FROM likes
                           GROUP BY film_id) as t ON f.id = t.film_id
               LEFT JOIN ratings as r ON f.rating_id = r.id
               ORDER BY t.cnt_likes DESC
               """;

        List<Film> films = jdbc.query(query, filmRowMapper, userId, friendId);

        setAllGenresDirectorsAndLikes(films);

        return films;
    }

    @Override
    public List<Film> getRecommendationsByUserId(Long id) {

        String similarUsersQuery = """
                SELECT l2.user_id,
                    COUNT(*) AS common_likes
                FROM likes AS l1
                JOIN likes AS l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ?
                    AND l2.user_id != ?
                GROUP BY l2.user_id
                ORDER BY common_likes DESC
                """;
        String recommendFilmsQuery = """
                SELECT film_id
                FROM likes
                WHERE user_id = ?
                AND film_id NOT IN (
                    SELECT film_id FROM likes WHERE user_id = ?
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
                .map(filmId -> findById(filmId).get())
                .toList();
    }

    @Override
    public List<Film> searchFilms(String substring, String queryParam) {
        StringBuilder query = new StringBuilder("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   r.id as mpa_id, r.name as mpa_name
            FROM films as f
            LEFT JOIN likes as l ON f.id = l.film_id
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
            ORDER BY COUNT(l.user_id) DESC
            """);

        List<Film> films = jdbc.query(query.toString(), filmRowMapper, params.toArray());

        setAllGenresDirectorsAndLikes(films);

        return films;
    }

    private void setAllGenresDirectorsAndLikes(List<Film> films) {
        Map<Long, Set<Genre>> allGenres = getGenresForAllFilms();
        films.forEach(film -> film.setGenres(allGenres.getOrDefault(film.getId(), Collections.emptySet())));

        Map<Long, Set<Director>> allDirectors = getDirectorsForAllFilms();
        films.forEach(film -> film.setDirectors(allDirectors.getOrDefault(film.getId(), Collections.emptySet())));

        Map<Long, Set<LikesDto>> allLikes = getLikesForAllFilms();
        films.forEach(film -> film.setLikes(allLikes.getOrDefault(film.getId(), Collections.emptySet())));
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
                WHERE r.id = ?
                """;

        try {
            Mpa mpa = jdbc.queryForObject(query, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Set<LikesDto> getLikesForFilmId(Long id) {
        String query = """
                SELECT u.id, u.email, u.login
                FROM users as u
                INNER JOIN likes as l ON u.id = l.user_id
                INNER JOIN films as f ON f.id = l.film_id AND f.id = ?
                """;

        Set<LikesDto> likes = new HashSet<>();

        jdbc.query(query, (rs) -> {
            LikesDto likesDto = new LikesDto();
            likesDto.setId(rs.getLong("id"));
            likesDto.setEmail(rs.getString("email"));
            likesDto.setLogin(rs.getString("login"));

            likes.add(likesDto);
        }, id);

        return likes;
    }

    private Map<Long, Set<LikesDto>> getLikesForAllFilms() {
        String query = """
                SELECT f.id as film_id, u.id, u.email, u.login, l.created_at
                FROM users as u
                INNER JOIN likes as l ON u.id = l.user_id
                INNER JOIN films as f ON f.id = l.film_id
                """;

        Map<Long, Set<LikesDto>> allLikes = new HashMap<>();

        jdbc.query(query, (rs) -> {
            Long filmId = rs.getLong("film_id");
            Set<LikesDto> likes = allLikes.computeIfAbsent(filmId, k -> new HashSet<>());

            LikesDto likesDto = new LikesDto();
            likesDto.setId(rs.getLong("id"));
            likesDto.setEmail(rs.getString("email"));
            likesDto.setLogin(rs.getString("login"));

            likes.add(likesDto);
        });

        return allLikes;
    }
}