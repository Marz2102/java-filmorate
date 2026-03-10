package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public interface UserStorage {
    Optional<User> findById(Long id);

    List<User> getUsers();

    User addUser(User user);

    User updateUser(User user);

    User addFriend(Long userId, Long friendId);

    User deleteFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId1, Long userId2);

    //Добавил дефолтное определение метода, чтобы не имплементировать его в UserDbStorage
    //(в принципе он вообще не нужен в новой реализации)
    default Long generateNextId() {
        Random random = new Random();
        return random.nextLong();
    };
}
