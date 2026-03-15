package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.mpaDto.MpaDto;
import ru.yandex.practicum.filmorate.services.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {
    private final FilmService filmService;

    public MpaController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaDto> getMpaById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение рейтинга по id");
        return ResponseEntity.ok(filmService.getMpaById(id));
    }

    @GetMapping
    public ResponseEntity<List<MpaDto>> getAllMpa() {
        log.info("Вызван эндпоинт на получение всех рейтингов");
        return ResponseEntity.ok(filmService.getAllMpa());
    }
}
