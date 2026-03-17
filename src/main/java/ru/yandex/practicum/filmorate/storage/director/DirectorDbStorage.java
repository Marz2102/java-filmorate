package ru.yandex.practicum.filmorate.storage.director;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Transactional
@Repository("DirectorDao")
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbc, DirectorRowMapper directorRowMapper) {
        this.jdbc = jdbc;
        this.directorRowMapper = directorRowMapper;
    }

    @Override
    public Optional<Director> findById(Long id) {
        String query = "SELECT id, name FROM directors WHERE id = ?";

        try {
            Director director = jdbc.queryForObject(query, directorRowMapper, id);
            return Optional.ofNullable(director);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Director> getDirectors() {
        String query = "SELECT * FROM directors";

        return jdbc.query(query, directorRowMapper);
    }

    @Override
    public Director addDirector(Director director) {
        String query = "INSERT INTO directors (name) VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Long id = (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        if (id != null) {
            director.setId(id);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить данные");
        }

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String query = "UPDATE directors SET name = ? WHERE id = ?";

        int rowsUpdated = jdbc.update(query,
                director.getName(),
                director.getId());

        if (rowsUpdated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обновить данные");
        }

        return director;
    }

    @Override
    public void deleteDirector(Long id) {
        String query = "DELETE FROM directors WHERE id = ?";

        jdbc.update(query, id);
    }
}
