package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository("MpaDao")
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    public MpaDbStorage(DataSource dataSource, MpaRowMapper mpaRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.mpaRowMapper = mpaRowMapper;
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        String query = "SELECT id, name FROM ratings WHERE id = ?";

        try {
            Mpa mpa = jdbc.queryForObject(query, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Mpa> getAllMpa() {
        String query = "SELECT id, name FROM ratings ORDER BY id";
        return jdbc.query(query, mpaRowMapper);
    }
}
