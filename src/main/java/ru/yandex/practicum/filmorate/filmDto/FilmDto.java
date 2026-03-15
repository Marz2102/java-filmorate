package ru.yandex.practicum.filmorate.filmDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.genreDto.GenreDto;
import ru.yandex.practicum.filmorate.mpaDto.MpaDto;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private List<GenreDto> genres;
    private MpaDto mpa;
}
