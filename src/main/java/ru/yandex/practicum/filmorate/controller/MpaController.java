package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaDto> getMpaById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение рейтинга по id");
        return ResponseEntity.ok(mpaService.getMpaById(id));
    }

    @GetMapping
    public ResponseEntity<List<MpaDto>> getAllMpa() {
        log.info("Вызван эндпоинт на получение всех рейтингов");
        return ResponseEntity.ok(mpaService.getAllMpa());
    }
}
