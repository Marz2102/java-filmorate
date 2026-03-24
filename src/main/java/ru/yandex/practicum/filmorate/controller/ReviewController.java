package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение отзыва по id");
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews(
            @RequestParam(name = "filmId", required = false) Long filmId,
            @RequestParam(name = "count", required = false, defaultValue = "10") int count
    ) {
        log.info("Вызван эндпоинт на получение всех отзывов");
        return ResponseEntity.ok(reviewService.getReviews(filmId, count));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> addReview(@Valid @RequestBody ReviewCreateDto review) {
        log.info("Вызван эндпоинт на создание нового отзыва");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewService.addReview(review));
    }

    @PutMapping
    public ResponseEntity<ReviewDto> updateReview(@Valid @RequestBody ReviewUpdateDto review) {
        log.info("Вызван эндпоинт на обновление данных отзыва");
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public ResponseEntity<ReviewDto> addLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на добавление лайка отзыву");
        return ResponseEntity.ok(reviewService.addLike(reviewId, userId));
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public ResponseEntity<ReviewDto> addDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на добавление дизлайка отзыву");
        return ResponseEntity.ok(reviewService.addDislike(reviewId, userId));
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public ResponseEntity<ReviewDto> deleteLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на удаление лайка отзыву");
        return ResponseEntity.ok(reviewService.deleteLike(reviewId, userId));
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public ResponseEntity<ReviewDto> deleteDislike(@PathVariable Long reviewId, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на удаление дизлайка отзыву");
        return ResponseEntity.ok(reviewService.deleteDislike(reviewId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.info("Вызван эндпоинт на удаление отзыва");
        reviewService.deleteReviewById(id);
        return ResponseEntity.noContent().build();
    }
}
