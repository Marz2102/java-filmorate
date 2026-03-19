package ru.yandex.practicum.filmorate.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {
    private Long reviewId;
    private String content;
    private Long filmId;
    private Long userId;
    private Boolean isPositive;
    private Integer useful;
}
