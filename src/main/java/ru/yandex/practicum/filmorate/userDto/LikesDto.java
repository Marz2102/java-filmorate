package ru.yandex.practicum.filmorate.userDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikesDto {
    private Long id;
    private String email;
    private String login;
    private LocalDateTime createdAt;
}
