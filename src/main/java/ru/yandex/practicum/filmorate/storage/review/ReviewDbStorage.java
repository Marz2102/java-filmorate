package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository("ReviewDao")
@Transactional
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper reviewRowMapper;
    private final EventStorage eventStorage;

    public ReviewDbStorage(JdbcTemplate jdbc, ReviewRowMapper reviewRowMapper,
                           @Qualifier("EventDao") final EventStorage eventStorage) {
        this.jdbc = jdbc;
        this.reviewRowMapper = reviewRowMapper;
        this.eventStorage = eventStorage;
    }

    @Override
    public Optional<Review> findById(Long id) {
        String query = "SELECT * FROM film_reviews WHERE id = ?";

        try {
            Review review = jdbc.queryForObject(query, reviewRowMapper, id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviews() {
        String query = "SELECT * FROM film_reviews ORDER BY useful DESC";

        return jdbc.query(query, reviewRowMapper);
    }

    @Override
    public void deleteReview(Long id) {
        String query = "DELETE FROM film_reviews WHERE id = ?";

        Review review = findById(id).get();
        jdbc.update(query, id);
        eventStorage.addEvent(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.REMOVE);
    }

    @Override
    public Review addReview(Review review) {
        String query = "INSERT INTO film_reviews (content, is_positive, film_id, user_id) VALUES (?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getFilmId());
            ps.setLong(4, review.getUserId());
            return ps;
        }, keyHolder);

        Long id = (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        if (id != null) {
            review.setReviewId(id);
            eventStorage.addEvent(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.ADD);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить данные");
        }

        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String query = "UPDATE film_reviews SET is_positive = ?, content = ? WHERE id = ?";

        int rowsUpdated = jdbc.update(
                query,
                review.getIsPositive(),
                review.getContent(),
                review.getReviewId()
        );

        if (rowsUpdated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обновить данные");
        }

        eventStorage.addEvent(review.getUserId(), review.getReviewId(), EventType.REVIEW, Operation.UPDATE);

        return review;
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId) {
        String query = "SELECT * FROM film_reviews WHERE film_id = ? ORDER BY useful DESC";

        return jdbc.query(query, reviewRowMapper, filmId);
    }

    @Override
    public Review addLike(Long reviewId, Long userId) {
        String selectQuery = "SELECT is_like FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        String insertQuery = "INSERT INTO reviews_reactions (review_id, user_id, is_like) VALUES (?, ?, ?)";
        String updateReactionQuery = "UPDATE reviews_reactions SET is_like = ? WHERE review_id = ? AND user_id = ?";
        String updateUsefulQuery = "UPDATE film_reviews SET useful = useful + ? WHERE id = ?";

        List<Boolean> result = jdbc.query(
                selectQuery,
                (rs, rowNum) -> rs.getBoolean("is_like"),
                reviewId,
                userId
        );

        int delta;

        if (result.isEmpty()) {
            jdbc.update(insertQuery, reviewId, userId, true);
            delta = 1;
        } else {
            Boolean isLike = result.getFirst();
            if (Boolean.TRUE.equals(isLike)) {
                return findById(reviewId).orElse(null);
            } else {
                jdbc.update(updateReactionQuery, true, reviewId, userId);
                delta = 2;
            }
        }

        jdbc.update(updateUsefulQuery, delta, reviewId);
        return findById(reviewId).orElse(null);
    }

    @Override
    public Review deleteLike(Long reviewId, Long userId) {
        String selectQuery = "SELECT is_like FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        String deleteQuery = "DELETE FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        String updateUsefulQuery = "UPDATE film_reviews SET useful = useful + ? WHERE id = ?";

        List<Boolean> result = jdbc.query(
                selectQuery,
                (rs, rowNum) -> rs.getBoolean("is_like"),
                reviewId,
                userId
        );

        if (result.isEmpty() || !Boolean.TRUE.equals(result.getFirst())) {
            return findById(reviewId).orElse(null);
        }

        jdbc.update(deleteQuery, reviewId, userId);
        jdbc.update(updateUsefulQuery, -1, reviewId);

        return findById(reviewId).orElse(null);
    }

    @Override
    public Review addDislike(Long reviewId, Long userId) {
        String selectQuery = "SELECT is_like FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        String insertQuery = "INSERT INTO reviews_reactions (review_id, user_id, is_like) VALUES (?, ?, ?)";
        String updateReactionQuery = "UPDATE reviews_reactions SET is_like = ? WHERE review_id = ? AND user_id = ?";
        String updateUsefulQuery = "UPDATE film_reviews SET useful = useful + ? WHERE id = ?";

        List<Boolean> result = jdbc.query(
                selectQuery,
                (rs, rowNum) -> rs.getBoolean("is_like"),
                reviewId,
                userId
        );

        int delta;

        if (result.isEmpty()) {
            jdbc.update(insertQuery, reviewId, userId, false);
            delta = -1;
        } else {
            Boolean isLike = result.getFirst();
            if (Boolean.FALSE.equals(isLike)) {
                return findById(reviewId).orElse(null);
            } else {
                jdbc.update(updateReactionQuery, false, reviewId, userId);
                delta = -2;
            }
        }

        jdbc.update(updateUsefulQuery, delta, reviewId);
        return findById(reviewId).orElse(null);
    }

    @Override
    public Review deleteDislike(Long reviewId, Long userId) {
        String selectQuery = "SELECT is_like FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        String deleteQuery = "DELETE FROM reviews_reactions WHERE review_id = ? AND user_id = ?";
        String updateUsefulQuery = "UPDATE film_reviews SET useful = useful + ? WHERE id = ?";

        List<Boolean> result = jdbc.query(
                selectQuery,
                (rs, rowNum) -> rs.getBoolean("is_like"),
                reviewId,
                userId
        );

        if (result.isEmpty() || !Boolean.FALSE.equals(result.getFirst())) {
            return findById(reviewId).orElse(null);
        }

        jdbc.update(deleteQuery, reviewId, userId);
        jdbc.update(updateUsefulQuery, 1, reviewId);

        return findById(reviewId).orElse(null);
    }
}
