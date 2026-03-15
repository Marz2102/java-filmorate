package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Optional<User> findById(Long id);

    List<User> getUsers();

    User addUser(User user);

    User updateUser(User user);

    User addFriend(Long userId, Long friendId);

    User deleteFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId1, Long userId2);
}
