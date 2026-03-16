package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Map<User, FriendshipStatus> friends = new HashMap<>();

    public void addFriend(Long id) {
        this.friends.add(id);
    }

    public void deleteFriend(Long id) {
        this.friends.remove(id);
    }
}
