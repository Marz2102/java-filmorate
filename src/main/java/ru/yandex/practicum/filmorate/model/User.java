package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    @NotBlank
    @Email(message = "Введите почту в корректном формате")
    private String email;

    @NotEmpty(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин должен состоять из одного слова, без пробелов")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть позже сегодняшнего дня")
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();

    public User(Long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public void addFriend(Long id) {
        this.friends.add(id);
    }

    public void deleteFriend(Long id) {
        this.friends.remove(id);
    }
}
