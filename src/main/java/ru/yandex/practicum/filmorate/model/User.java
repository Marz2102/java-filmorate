package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public void addFriend(Long id) {
        this.friends.add(id);
    }

    public void deleteFriend(Long id) {
        this.friends.remove(id);
    }
}
