package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Rating;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository("RatingDao")
public class RatingDbStorage implements RatingStorage {
    private final JdbcTemplate jdbc;
    private final RatingRowMapper ratingRowMapper;

    public RatingDbStorage(DataSource dataSource, RatingRowMapper ratingRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.ratingRowMapper = ratingRowMapper;
    }

    @Override
    public Optional<Rating> findById(Long id) {
        String query = "SELECT id, name FROM ratings WHERE id = ?";

        try {
            Rating rating = jdbc.queryForObject(query, ratingRowMapper, id);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Rating> getRatings() {
        String query = "SELECT id, name FROM ratings ORDER BY id DESC";
        return jdbc.query(query, ratingRowMapper);
    }
}
