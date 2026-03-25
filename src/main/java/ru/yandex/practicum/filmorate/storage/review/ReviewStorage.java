package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Optional<Review> findById(Long id);

    List<Review> getReviews();

    void deleteReview(Long id);

    Review addReview(Review review);

    Review updateReview(Review review);

    List<Review> getReviewsByFilmId(Long filmId);

    Review addLike(Long reviewId, Long userId);

    Review deleteLike(Long reviewId, Long userId);

    Review addDislike(Long reviewId, Long userId);

    Review deleteDislike(Long reviewId, Long userId);
}
