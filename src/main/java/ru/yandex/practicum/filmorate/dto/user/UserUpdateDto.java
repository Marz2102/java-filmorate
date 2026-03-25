package ru.yandex.practicum.filmorate.dto.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    @NotNull(message = "Укажите id в теле запроса")
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
}