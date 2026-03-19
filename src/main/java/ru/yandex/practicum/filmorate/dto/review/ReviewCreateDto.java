package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateDto {
    @NotEmpty(message = "Отзыв не может быть пустым")
    private String content;

    @NotNull
    private Long filmId;

    @NotNull
    private Long userId;

    @NotNull
    private Boolean isPositive;
}
