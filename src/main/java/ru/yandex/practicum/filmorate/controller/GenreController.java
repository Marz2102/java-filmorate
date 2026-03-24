package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getGenreById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение жанра по id");
        return ResponseEntity.ok(genreService.getGenreById(id));
    }

    @GetMapping
    public ResponseEntity<List<GenreDto>> getGenres() {
        log.info("Вызван эндпоинт на получение всех жанров");
        return ResponseEntity.ok(genreService.getGenres());
    }
}
