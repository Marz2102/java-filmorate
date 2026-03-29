package ru.yandex.practicum.filmorate.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikesDto {
    private Long id;
    private String email;
    private String login;
}
