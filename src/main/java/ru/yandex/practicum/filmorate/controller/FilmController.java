package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@Validated
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

    @PutMapping(value = {"/{id}/like/{userId}","/{id}/like/{userId}/{mark}"})
    public ResponseEntity<FilmDto> addMark(
            @PathVariable Long id,
            @PathVariable Long userId,
            @PathVariable(required = false) @Range(min = 1, max = 10, message = "Укажите оценку от 1 до 10 включительно") Double mark) {
        if (mark == null) {
            mark = 10.0;
        }
        log.info("Вызван эндпоинт на добавление оценки фильму");
        return ResponseEntity.ok(filmService.addMark(id, userId, mark));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<FilmDto> deleteMark(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Вызван эндпоинт на удаление оценки фильма");
        return ResponseEntity.ok(filmService.deleteMark(id, userId));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> getMostRatedFilms(
            @Positive(message = "Укажите положительный параметр count") @RequestParam(name = "count", required = false, defaultValue = "10") int count,
            @Positive(message = "Укажите положительный Id") @RequestParam(name = "genreId", required = false) Long genreId,
            @Positive(message = "Укажите положительный год") @RequestParam(name = "year", required = false) Integer year) {
        log.info("Вызван эндпоинт на получение списка самых популярных фильмов с фильтрацией по жанру и году");
        return ResponseEntity.ok(filmService.getMostRatedFilms(count, genreId, year));
    }

    @GetMapping("/director/{id}")
    public ResponseEntity<List<FilmDto>> getAllFilmsByDirectorId(
            @PathVariable Long id,
            @RequestParam(name = "sortBy") String sortBy
    ) {
        if (!"rate".equals(sortBy) && !"year".equals(sortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Параметр sortBy должен быть 'rate' или 'year'"
            );
        }

        log.info("Вызван эндпоинт на получение фильмов режиссёра {} с сортировкой {}", id, sortBy);
        return ResponseEntity.ok(filmService.getAllFilmsByDirectorId(id, sortBy));
    }

    @GetMapping("/common")
    public ResponseEntity<List<FilmDto>> getCommonFilms(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "friendId") Long friendId) {
        log.info("Вызван эндпоинт на получение списка общих фильмов для пользователей {} и {}", userId, friendId);
        return ResponseEntity.ok(filmService.getCommonFilms(userId, friendId));
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Long filmId) {
        log.info("Вызван эндпоинт на удаление фильма с id {}", filmId);
        filmService.deleteFilm(filmId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmDto>> searchFilms(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "by") String queryParam) {
        log.info("Вызван эндпоинт на поиск списка фильмов по подстроке {}", query);
        return ResponseEntity.ok(filmService.searchFilms(query, queryParam));
    }
}