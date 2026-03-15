package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository("GenreDao")
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    public GenreDbStorage(DataSource dataSource, GenreRowMapper genreRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Optional<Genre> findById(Long id) {
        String query = "SELECT id, name FROM genres WHERE id = ?";

        try {
            Genre genre = jdbc.queryForObject(query, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Genre> getGenres() {
        String query = "SELECT id, name FROM genres ORDER BY id DESC";
        return jdbc.query(query, genreRowMapper);
    }
}
