package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(final FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение фильма по id");
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @GetMapping
    public ResponseEntity<List<Film>> getFilms() {
        log.info("Вызван эндпоинт на получение всех фильмов");
        return ResponseEntity.ok(filmService.getFilms());
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        log.info("Вызван эндпоинт на создание нового фильма");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(filmService.addFilm(film));
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        log.info("Вызван эндпоинт на обновление данных фильма");
        return ResponseEntity.ok(filmService.updateFilm(film));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на добавление лайка");
        return ResponseEntity.ok(filmService.addLike(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Film> deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на удаление лайка");
        return ResponseEntity.ok(filmService.deleteLike(id, userId));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getMostLikedFilms(
            @RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        log.info("Вызван эндпоинт на получение списка самых популярных фильмов");
        return ResponseEntity.ok(filmService.getMostLikedFilms(count));
    }
}
