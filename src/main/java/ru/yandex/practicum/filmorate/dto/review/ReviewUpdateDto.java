package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewUpdateDto {
    private Long reviewId;

    @NotEmpty(message = "Отзыв не может быть пустым")
    private String content;

    private Long filmId;
    private Long userId;
    private Boolean isPositive;
}