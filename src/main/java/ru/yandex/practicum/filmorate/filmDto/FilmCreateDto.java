package ru.yandex.practicum.filmorate.filmDto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class FilmCreateDto {
    @NotEmpty(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание фильма не может быть больше 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private Integer duration;

    private List<FilmGenreDto> genres;

    private FilmMpaDto mpa;

    public Set<Long> getGenreIds() {
        if (genres == null) {
            return Collections.emptySet();
        }

        return genres.stream()
                .map(FilmGenreDto::getId)
                .collect(Collectors.toSet());
    }

    public Long getMpaId() {
        if (mpa == null) {
            return null;
        }

        return mpa.getId();
    }
}
