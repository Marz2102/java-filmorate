package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private ReviewDto reviewDto;
    private ReviewCreateDto reviewCreateDto;

    @BeforeEach
    void setUp() {
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
    void getReviewById_ShouldReturnReview() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(reviewDto);

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.content").value("Great movie!"))
                .andExpect(jsonPath("$.isPositive").value(true));
    }

    @Test
    void getAllReviews_WithoutFilmId_ShouldReturnAllReviews() throws Exception {
        when(reviewService.getReviews(null, 10)).thenReturn(List.of(reviewDto));

        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllReviews_WithFilmId_ShouldReturnReviewsForFilm() throws Exception {
        when(reviewService.getReviews(1L, 10)).thenReturn(List.of(reviewDto));

        mockMvc.perform(get("/reviews?filmId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllReviews_WithCount_ShouldLimitResults() throws Exception {
        when(reviewService.getReviews(null, 5)).thenReturn(List.of(reviewDto));

        mockMvc.perform(get("/reviews?count=5"))
                .andExpect(status().isOk());
    }

    @Test
    void addReview_ShouldReturnCreated() throws Exception {
        when(reviewService.addReview(any(ReviewCreateDto.class))).thenReturn(reviewDto);

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.content").value("Great movie!"));
    }

    @Test
    void addReview_WithEmptyContent_ShouldReturnBadRequest() throws Exception {
        ReviewCreateDto invalidDto = new ReviewCreateDto(
                "",
                1L,
                1L,
                true
        );

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).addReview(any());
    }

    @Test
    void addReview_WithNullFilmId_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
                {
                    "content": "Great movie!",
                    "filmId": null,
                    "userId": 1,
                    "isPositive": true
                }
                """;

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).addReview(any());
    }

    @Test
    void updateReview_ShouldReturnOk() throws Exception {
        ReviewUpdateDto updateDto = new ReviewUpdateDto(
                1L,
                "Updated review",
                1L,
                1L,
                false
        );

        ReviewDto updatedDto = new ReviewDto();
        updatedDto.setReviewId(1L);
        updatedDto.setContent("Updated review");
        updatedDto.setIsPositive(false);
        updatedDto.setFilmId(1L);
        updatedDto.setUserId(1L);
        updatedDto.setUseful(0);

        when(reviewService.updateReview(any(ReviewUpdateDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated review"))
                .andExpect(jsonPath("$.isPositive").value(false));
    }

    @Test
    void updateReview_WithEmptyContent_ShouldReturnBadRequest() throws Exception {
        ReviewUpdateDto invalidDto = new ReviewUpdateDto(
                1L,
                "",
                1L,
                1L,
                true
        );

        mockMvc.perform(put("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).updateReview(any());
    }

    @Test
    void addLike_ShouldReturnOk() throws Exception {
        when(reviewService.addLike(1L, 1L)).thenReturn(reviewDto);

        mockMvc.perform(put("/reviews/1/like/1"))
                .andExpect(status().isOk());

        verify(reviewService).addLike(1L, 1L);
    }

    @Test
    void addDislike_ShouldReturnOk() throws Exception {
        when(reviewService.addDislike(1L, 1L)).thenReturn(reviewDto);

        mockMvc.perform(put("/reviews/1/dislike/1"))
                .andExpect(status().isOk());

        verify(reviewService).addDislike(1L, 1L);
    }

    @Test
    void deleteLike_ShouldReturnOk() throws Exception {
        when(reviewService.deleteLike(1L, 1L)).thenReturn(reviewDto);

        mockMvc.perform(delete("/reviews/1/like/1"))
                .andExpect(status().isOk());

        verify(reviewService).deleteLike(1L, 1L);
    }

    @Test
    void deleteDislike_ShouldReturnOk() throws Exception {
        when(reviewService.deleteDislike(1L, 1L)).thenReturn(reviewDto);

        mockMvc.perform(delete("/reviews/1/dislike/1"))
                .andExpect(status().isOk());

        verify(reviewService).deleteDislike(1L, 1L);
    }

    @Test
    void deleteReview_ShouldReturnNoContent() throws Exception {
        doNothing().when(reviewService).deleteReviewById(1L);

        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReviewById(1L);
    }
}
