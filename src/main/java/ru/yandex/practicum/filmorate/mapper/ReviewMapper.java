package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.model.Review;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReviewMapper {

    public static Review mapCreateReviewDtoToReview(ReviewCreateDto reviewDto) {
        Review review = new Review();

        review.setIsPositive(reviewDto.getIsPositive());
        review.setContent(reviewDto.getContent());
        review.setFilmId(reviewDto.getFilmId());
        review.setUserId(reviewDto.getUserId());

        return review;
    }

    public static ReviewDto mapReviewToReviewDto(Review review) {
        ReviewDto reviewDto = new ReviewDto();

        reviewDto.setReviewId(review.getReviewId());
        reviewDto.setIsPositive(review.getIsPositive());
        reviewDto.setContent(review.getContent());
        reviewDto.setFilmId(review.getFilmId());
        reviewDto.setUserId(review.getUserId());
        reviewDto.setUseful(review.getUseful());

        return reviewDto;
    }

    public static Review updateReviewFields(ReviewUpdateDto reviewDto, Review review) {
        if (reviewDto.getIsPositive() != null) {
            review.setIsPositive(reviewDto.getIsPositive());
        }

        if (reviewDto.getContent() != null) {
            review.setContent(reviewDto.getContent());
        }

        return review;
    }
}
