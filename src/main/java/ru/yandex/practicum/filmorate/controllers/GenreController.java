package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.genreDto.GenreDto;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {
    private final FilmService filmService;

    public GenreController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getGenreById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение жанра по id");
        return ResponseEntity.ok(filmService.getGenreById(id));
    }

    @GetMapping
    public ResponseEntity<List<GenreDto>> getGenres() {
        log.info("Вызван эндпоинт на получение всех жанров");
        return ResponseEntity.ok(filmService.getGenres());
    }

}
