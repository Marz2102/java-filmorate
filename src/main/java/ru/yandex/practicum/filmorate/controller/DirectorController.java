package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/director")
public class DirectorController {

    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDto> getDirectorById(@PathVariable("id") Long id) {
        log.info("Вызван эндпоинт на получение режиссёра по id");
        return ResponseEntity.ok(directorService.getDirectorById(id));
    }

    @GetMapping
    public ResponseEntity<List<DirectorDto>> getAllDirector() {
        log.info("Вызван эндпоинт на получение всех режиссёров");
        return ResponseEntity.ok(directorService.getDirectors());
    }

    @PostMapping
    public ResponseEntity<DirectorDto> addDirector(@Valid @RequestBody DirectorDto director) {
        log.info("Вызван эндпоинт на создание нового режиссёра");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(directorService.addDirector(director));
    }

    @PutMapping
    public ResponseEntity<DirectorDto> updateDirector(@Valid @RequestBody DirectorUpdateDto director) {
        log.info("Вызван эндпоинт на обновление имени режиссёра");
        return ResponseEntity.ok(directorService.updateDirector(director));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(@PathVariable Long id) {
        log.info("Вызван эндпоинт на удаление режиссёра");
        directorService.deleteDirectorById(id);
        return ResponseEntity.noContent().build();
    }
}
