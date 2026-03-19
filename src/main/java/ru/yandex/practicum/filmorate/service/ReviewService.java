package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public ReviewService(@Qualifier("ReviewDao") final ReviewStorage reviewStorage, UserService userService, FilmService filmService) {
        this.reviewStorage = reviewStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        List<ReviewDto> reviews;

        if (filmId == null) {
            reviews = reviewStorage.getReviews().stream()
                    .map(ReviewMapper::mapReviewToReviewDto)
                    .toList();
        } else {
            reviews = reviewStorage.getReviewsByFilmId(filmId).stream()
                    .map(ReviewMapper::mapReviewToReviewDto)
                    .toList();
        }

        return reviews.stream().limit(count).toList();
    }

    public ReviewDto getReviewById(Long id) {
        return reviewStorage.findById(id)
                .map(ReviewMapper::mapReviewToReviewDto)
                .orElseThrow(() -> new NotFoundException("Отзыв с id - " + id + " не найден"));
    }

    public void deleteReviewById(Long id) {
        reviewStorage.deleteReview(id);
    }

    public ReviewDto addReview(ReviewCreateDto reviewDto) {
        userService.getUserById(reviewDto.getUserId());
        filmService.getFilmById(reviewDto.getFilmId());

        Review review = ReviewMapper.mapCreateReviewDtoToReview(reviewDto);

        review = reviewStorage.addReview(review);

        return ReviewMapper.mapReviewToReviewDto(review);
    }

    public ReviewDto addLike(Long reviewId, Long userId) {
        checkReviewAndUserExists(reviewId, userId);

        return ReviewMapper.mapReviewToReviewDto(reviewStorage.addLike(reviewId, userId));
    }

    public ReviewDto deleteLike(Long reviewId, Long userId) {
        checkReviewAndUserExists(reviewId, userId);

        return ReviewMapper.mapReviewToReviewDto(reviewStorage.deleteLike(reviewId, userId));
    }

    public ReviewDto addDislike(Long reviewId, Long userId) {
        checkReviewAndUserExists(reviewId, userId);

        return ReviewMapper.mapReviewToReviewDto(reviewStorage.addDislike(reviewId, userId));
    }

    public ReviewDto deleteDislike(Long reviewId, Long userId) {
        checkReviewAndUserExists(reviewId, userId);

        return ReviewMapper.mapReviewToReviewDto(reviewStorage.deleteDislike(reviewId, userId));
    }

    private void checkReviewAndUserExists(Long reviewId, Long userId) {
        userService.getUserById(userId);
        getReviewById(reviewId);
    }

    public ReviewDto updateReview(ReviewUpdateDto reviewUpdateDto) {
        userService.getUserById(reviewUpdateDto.getUserId());
        filmService.getFilmById(reviewUpdateDto.getFilmId());
        System.out.println(reviewUpdateDto);
        Review updatedReview = reviewStorage.findById(reviewUpdateDto.getReviewId())
                .map(review -> ReviewMapper.updateReviewFields(reviewUpdateDto, review))
                .orElseThrow(() -> new NotFoundException("Отзыв с id - " + reviewUpdateDto.getReviewId() + " не найден"));

        return ReviewMapper.mapReviewToReviewDto(reviewStorage.updateReview(updatedReview));
    }
}
