package ru.yandex.practicum.filmorate.dto.director;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectorUpdateDto {
    @NotNull(message = "Укажите id в теле запроса")
    private Long id;

    @NotEmpty(message = "Имя режиссёра не может быть пустым")
    private String name;
}
