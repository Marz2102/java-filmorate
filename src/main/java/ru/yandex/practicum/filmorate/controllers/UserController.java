package ru.yandex.practicum.filmorate.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.services.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        log.info("Вызван эндпоинт на получение всех пользователей");
        return ResponseEntity.ok(userService.getUsers());
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        log.info("Вызван эндпоинт на создание нового пользователя");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.addUser(user));
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        log.info("Вызван эндпоинт на обновление данных пользователя");
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Вызван эндпоинт на добавление друга");
        return ResponseEntity.ok(userService.addFriend(id, friendId));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Вызван эндпоинт на удаление друга");
        return ResponseEntity.ok(userService.deleteFriend(id, friendId));
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long id) {
        log.info("Вызван эндпоинт на получение списка всех друзей");
        return ResponseEntity.ok(userService.getFriends(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Вызван эндпоинт на получение списка общих друзей у двух пользователей");
        return ResponseEntity.ok(userService.getCommonFriends(id, otherId));
    }

    @PostMapping("/clear")
    public ResponseEntity<List<User>> clearStorage() {
        log.info("Вызван эндпоинт на полное удаление базы пользователей (для теста)");
        return ResponseEntity.ok(userService.clearStorage());
    }
}