package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.FilmService;

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
    public ResponseEntity<FilmDto> getFilmById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение фильма по id");
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getFilms() {
        log.info("Вызван эндпоинт на получение всех фильмов");
        return ResponseEntity.ok(filmService.getFilms());
    }

    @PostMapping
    public ResponseEntity<FilmDto> addFilm(@Valid @RequestBody FilmCreateDto film) {
        log.info("Вызван эндпоинт на создание нового фильма");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(filmService.addFilm(film));
    }

    @PutMapping
    public ResponseEntity<FilmDto> updateFilm(@Valid @RequestBody FilmUpdateDto film) {
        log.info("Вызван эндпоинт на обновление данных фильма");
        return ResponseEntity.ok(filmService.updateFilm(film));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDto> addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на добавление лайка");
        return ResponseEntity.ok(filmService.addLike(id, userId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDto> deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на удаление лайка");
        return ResponseEntity.ok(filmService.deleteLike(id, userId));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> getMostLikedFilms(
            @RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        log.info("Вызван эндпоинт на получение списка самых популярных фильмов");
        return ResponseEntity.ok(filmService.getMostLikedFilms(count));
    }

    @GetMapping("/director/{id}")
    public ResponseEntity<List<FilmDto>> getAllFilmsByDirectorId(
            @PathVariable Long id,
            @RequestParam(name = "sortBy") String sortBy
    ) {
        if (!"likes".equals(sortBy) && !"year".equals(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Параметр sortBy должен быть 'likes' или 'year'"
            );
        }

        log.info("Вызван эндпоинт на получение фильмов режиссёра {} с сортировкой {}", id, sortBy);
        return ResponseEntity.ok(filmService.getAllFilmsByDirectorId(id, sortBy));
    }
}
