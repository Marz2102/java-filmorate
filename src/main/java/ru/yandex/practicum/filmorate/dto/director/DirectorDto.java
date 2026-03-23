package ru.yandex.practicum.filmorate.dto.director;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectorDto {
    private Long id;

    @NotEmpty(message = "Имя режиссёра не может быть пустым")
    private String name;
}