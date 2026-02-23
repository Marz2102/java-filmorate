package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.Exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/user")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public List<User> getUsers() {
        log.info("Вызван эндпоинт на получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Вызван эндпоинт на создание нового пользователя");

        if (user == null) {
            log.warn("Пустой запрос");
            throw new ValidationException("Запрос некорректен");
        }

        Long id = generateNextId();
        log.debug("Сгенерирован новый id - {}", id);
        user.setId(id);

        if (user.getName() == null) {
            user.setName(user.getLogin());
            log.debug("Имя пусто, используется логин - {}", user.getName());
        }

        users.put(user.getId(), user);
        log.info("Успешно добавлен новый пользователь");

        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Вызван эндпоинт на обновление данных пользователя");

        validateRequestBody(user);
        log.trace("Валидация запроса прошла успешно");

        User oldUser = users.get(user.getId());

        if (user.getName() != null) {
            oldUser.setName(user.getName());
            log.debug("Обновили имя пользователя - {}", oldUser.getName());
        }

        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
            log.debug("Обновили дату рождения пользователя - {}", oldUser.getBirthday());
        }

        log.info("Данные пользователя успешно обновлены");

        return oldUser;
    }

    private void validateRequestBody(User user) {
        if (user == null) {
            log.warn("Пустой запрос");
            throw new ValidationException("Запрос некорректен");
        }

        if (user.getId() == null) {
            log.warn("Отсутствует id");
            throw new ValidationException("Укажите id для обновления пользователя");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Проблема с полем 'Дата рождения'");
            throw new ValidationException("День рождения не может быть позже сегодняшней даты");
        }
    }

    private Long generateNextId() {
        log.trace("Генерация нового id");
        long currentId = users
                .keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);

        return currentId + 1;
    }
}