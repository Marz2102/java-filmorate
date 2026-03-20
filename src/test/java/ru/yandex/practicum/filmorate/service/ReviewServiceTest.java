package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewStorage reviewStorage;

    @Mock
    private UserService userService;

    @Mock
    private FilmService filmService;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private ReviewDto reviewDto;
    private ReviewCreateDto reviewCreateDto;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setReviewId(1L);
        review.setContent("Great movie!");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);
        review.setUseful(0);

        reviewDto = new ReviewDto();
        reviewDto.setReviewId(1L);
        reviewDto.setContent("Great movie!");
        reviewDto.setIsPositive(true);
        reviewDto.setFilmId(1L);
        reviewDto.setUserId(1L);
        reviewDto.setUseful(0);

        reviewCreateDto = new ReviewCreateDto(
                "Great movie!",
                1L,
                1L,
                true
        );
    }

    @Test
    void getReviews_WithoutFilmId_ShouldReturnAllReviews() {
        when(reviewStorage.getReviews()).thenReturn(List.of(review));

        List<ReviewDto> result = reviewService.getReviews(null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Great movie!");

        verify(reviewStorage).getReviews();
    }

    @Test
    void getReviews_WithFilmId_ShouldReturnReviewsForFilm() {
        when(reviewStorage.getReviewsByFilmId(1L)).thenReturn(List.of(review));

        List<ReviewDto> result = reviewService.getReviews(1L, 10);

        assertThat(result).hasSize(1);

        verify(reviewStorage).getReviewsByFilmId(1L);
    }

    @Test
    void getReviews_WithCount_ShouldLimitResults() {
        Review review2 = new Review();
        review2.setReviewId(2L);
        review2.setContent("Second review");
        review2.setIsPositive(false);
        review2.setFilmId(1L);
        review2.setUserId(2L);
        review2.setUseful(0);

        when(reviewStorage.getReviews()).thenReturn(List.of(review, review2));

        List<ReviewDto> result = reviewService.getReviews(null, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReviewId()).isEqualTo(1L);

        verify(reviewStorage).getReviews();
    }

    @Test
    void getReviewById_ShouldReturnReview() {
        when(reviewStorage.findById(1L)).thenReturn(Optional.of(review));

        ReviewDto result = reviewService.getReviewById(1L);

        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Great movie!");

        verify(reviewStorage).findById(1L);
    }

    @Test
    void getReviewById_NotFound_ShouldThrowException() {
        when(reviewStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.getReviewById(999L));

        verify(reviewStorage).findById(999L);
    }

    @Test
    void addReview_ShouldReturnSavedReview() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(filmService.getFilmById(1L)).thenReturn(null);
        when(reviewStorage.addReview(any(Review.class))).thenReturn(review);

        ReviewDto result = reviewService.addReview(reviewCreateDto);

        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Great movie!");

        verify(userService).getUserById(1L);
        verify(filmService).getFilmById(1L);
        verify(reviewStorage).addReview(any(Review.class));
    }

    @Test
    void updateReview_ShouldReturnUpdatedReview() {
        ReviewUpdateDto updateDto = new ReviewUpdateDto(
                1L,
                "Updated review",
                1L,
                1L,
                false
        );

        Review updatedReview = new Review();
        updatedReview.setReviewId(1L);
        updatedReview.setContent("Updated review");
        updatedReview.setIsPositive(false);
        updatedReview.setFilmId(1L);
        updatedReview.setUserId(1L);
        updatedReview.setUseful(0);

        when(userService.getUserById(1L)).thenReturn(null);
        when(filmService.getFilmById(1L)).thenReturn(null);
        when(reviewStorage.findById(1L)).thenReturn(Optional.of(review));
        when(reviewStorage.updateReview(any(Review.class))).thenReturn(updatedReview);

        ReviewDto result = reviewService.updateReview(updateDto);

        assertThat(result.getContent()).isEqualTo("Updated review");
        assertThat(result.getIsPositive()).isFalse();

        verify(userService).getUserById(1L);
        verify(filmService).getFilmById(1L);
        verify(reviewStorage).findById(1L);
        verify(reviewStorage).updateReview(any(Review.class));
    }

    @Test
    void updateReview_NotFound_ShouldThrowException() {
        ReviewUpdateDto updateDto = new ReviewUpdateDto(
                999L,
                "Updated review",
                1L,
                1L,
                false
        );

        when(userService.getUserById(1L)).thenReturn(null);
        when(filmService.getFilmById(1L)).thenReturn(null);
        when(reviewStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.updateReview(updateDto));

        verify(reviewStorage).findById(999L);
        verify(reviewStorage, never()).updateReview(any());
    }

    @Test
    void deleteReviewById_ShouldCallStorage() {
        doNothing().when(reviewStorage).deleteReview(1L);

        reviewService.deleteReviewById(1L);

        verify(reviewStorage).deleteReview(1L);
    }

    @Test
    void addLike_ShouldAddLike() {
        Review likedReview = new Review();
        likedReview.setReviewId(1L);
        likedReview.setUseful(1);

        when(userService.getUserById(1L)).thenReturn(null);
        when(reviewStorage.findById(1L)).thenReturn(Optional.of(review));
        when(reviewStorage.addLike(1L, 1L)).thenReturn(likedReview);

        ReviewDto result = reviewService.addLike(1L, 1L);

        assertThat(result.getUseful()).isEqualTo(1);

        verify(userService).getUserById(1L);
        verify(reviewStorage).findById(1L);
        verify(reviewStorage).addLike(1L, 1L);
    }

    @Test
    void deleteLike_ShouldDeleteLike() {
        Review reviewAfterDelete = new Review();
        reviewAfterDelete.setReviewId(1L);
        reviewAfterDelete.setUseful(0);

        when(userService.getUserById(1L)).thenReturn(null);
        when(reviewStorage.findById(1L)).thenReturn(Optional.of(review));
        when(reviewStorage.deleteLike(1L, 1L)).thenReturn(reviewAfterDelete);

        ReviewDto result = reviewService.deleteLike(1L, 1L);

        assertThat(result.getUseful()).isEqualTo(0);

        verify(userService).getUserById(1L);
        verify(reviewStorage).findById(1L);
        verify(reviewStorage).deleteLike(1L, 1L);
    }

    @Test
    void addDislike_ShouldAddDislike() {
        Review dislikedReview = new Review();
        dislikedReview.setReviewId(1L);
        dislikedReview.setUseful(-1);

        when(userService.getUserById(1L)).thenReturn(null);
        when(reviewStorage.findById(1L)).thenReturn(Optional.of(review));
        when(reviewStorage.addDislike(1L, 1L)).thenReturn(dislikedReview);

        ReviewDto result = reviewService.addDislike(1L, 1L);

        assertThat(result.getUseful()).isEqualTo(-1);

        verify(userService).getUserById(1L);
        verify(reviewStorage).findById(1L);
        verify(reviewStorage).addDislike(1L, 1L);
    }

    @Test
    void deleteDislike_ShouldDeleteDislike() {
        Review reviewAfterDelete = new Review();
        reviewAfterDelete.setReviewId(1L);
        reviewAfterDelete.setUseful(0);

        when(userService.getUserById(1L)).thenReturn(null);
        when(reviewStorage.findById(1L)).thenReturn(Optional.of(review));
        when(reviewStorage.deleteDislike(1L, 1L)).thenReturn(reviewAfterDelete);

        ReviewDto result = reviewService.deleteDislike(1L, 1L);

        assertThat(result.getUseful()).isEqualTo(0);

        verify(userService).getUserById(1L);
        verify(reviewStorage).findById(1L);
        verify(reviewStorage).deleteDislike(1L, 1L);
    }
}