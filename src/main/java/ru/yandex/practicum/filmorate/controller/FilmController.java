package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/film")
public class FilmController {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getFilms() {
        log.info("Вызван эндпоинт на получение всех фильмов");
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Вызван эндпоинт на создание нового фильма");

        if (film == null) {
            log.warn("Пустой запрос");
            throw new ValidationException("Запрос некорректен");
        }

        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Проблема с полем 'Дата релиза'");
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }

        Long id = generateNextId();
        log.debug("Сгенерирован новый id - {}", id);
        film.setId(id);

        films.put(film.getId(), film);
        log.info("Успешно добавлен новый пользователь");

        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Вызван эндпоинт на обновление данных фильма");

        validateRequestBody(film);
        log.trace("Валидация запроса прошла успешно");

        Film oldFilm = films.get(film.getId());

        if (film.getDescription() != null) {
            oldFilm.setDescription(film.getDescription());
            log.debug("Обновили описание фильма - {}", oldFilm.getDescription());
        }

        log.info("Данные фильма успешно обновлены");

        return oldFilm;
    }

    private void validateRequestBody(Film film) {
        if (film == null || film.getId() == null) {
            log.warn("Пустой запрос");
            throw new ValidationException("Запрос некорректен");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Проблема с полем 'Описание'");
            throw new ValidationException("Описание фильма не может быть больше 200 символов");
        }
    }

    private Long generateNextId() {
        log.trace("Генерация нового id");
        long currentId = films
                .keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);

        return currentId + 1;
    }
}
