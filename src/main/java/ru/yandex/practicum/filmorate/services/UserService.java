package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final InMemoryUserStorage userStorage;

    public UserService(final InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(final User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(final User user) {
        return userStorage.updateUser(user);
    }

    public User addFriend(final User user, final User friend) {
        return userStorage.addFriend(user.getId(), friend.getId());
    }

    public User deleteFriend(final User user, final User friend) {
        return userStorage.deleteFriend(user.getId(), friend.getId());
    }

    public List<User> getFriends(final User user) {
        return userStorage.getFriends(user.getId());
    }

    public List<User> getCommonFriends(final User user1, final User user2) {
        return userStorage.getCommonFriends(user1.getId(), user2.getId());
    }

    private void validateRequestBody(User user) {
        if (user.getId() == null) {
            log.warn("Отсутствует id");
            throw new ValidationException("Укажите id для обновления пользователя");
        }
    }
}
