package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private Long id;

    @NotNull(message = "Название фильма не может отсутствовать")
    @NotEmpty(message = "Название фильма не может быть пустым")
    private final String name;

    @Size(max = 200, message = "Описание фильма не может быть больше 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может отсутствовать")
    private final LocalDate releaseDate;

    @NotNull(message = "Продолжительность фильма не может отсутствовать")
    @PositiveOrZero(message = "Продолжительность фильма не может быть отрицательной")
    private final Duration duration;
}
