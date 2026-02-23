package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Long id;

    @NotNull(message = "Введите почту в корректном формате")
    @Email(message = "Введите почту в корректном формате")
    private final String email;

    @NotNull(message = "Укажите логин из одного слова и без пробелов")
    @NotEmpty(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин должен состоять из одного слова, без пробелов")
    private final String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть позже сегодняшнего дня")
    private LocalDate birthday;
}
