package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.ratingDto.RatingDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RatingMapper {

    public static Rating mapRatingDtoToRating(RatingDto ratingDto) {
        Rating rating = new Rating();
        rating.setId(ratingDto.getId());
        rating.setName(ratingDto.getName());

        return rating;
    }

    public static RatingDto mapToRatingDto(Rating rating) {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setId(rating.getId());
        ratingDto.setName(rating.getName());

        return ratingDto;
    }

}