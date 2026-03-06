package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.Exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(final UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User getUserById(Long id) {
        checkToFindById(id);
        return userStorage.findById(id).get();
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(final User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.debug("Имя пусто, используется логин - {}", user.getName());
        }

        return userStorage.addUser(user);
    }

    public User updateUser(final User user) {
        validateRequestBody(user);
        log.info("Валидация запроса прошла успешно");

        checkToFindById(user.getId());
        User oldUser = userStorage.findById(user.getId()).get();

        if (user.getName() != null && !user.getName().isEmpty()) {
            oldUser.setName(user.getName());
            log.debug("Обновили имя пользователя - {}", oldUser.getName());
        }

        if (user.getBirthday() != null) {
            oldUser.setBirthday(user.getBirthday());
            log.debug("Обновили дату рождения пользователя - {}", oldUser.getBirthday());
        }

        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
            log.debug("Обновили почту пользователя - {}", oldUser.getEmail());
        }

        if (user.getLogin() != null) {
            oldUser.setLogin(user.getLogin());
            log.debug("Обновили логин пользователя - {}", oldUser.getLogin());
        }

        return userStorage.updateUser(oldUser);
    }

    public User addFriend(Long id, Long friendId) {
        checkToFindById(id);
        checkToFindById(friendId);

        return userStorage.addFriend(id, friendId);
    }

    public User deleteFriend(Long id, Long friendId) {
        checkToFindById(id);
        checkToFindById(friendId);

        return userStorage.deleteFriend(id, friendId);
    }

    public List<User> getFriends(Long id) {
        checkToFindById(id);
        return userStorage.getFriends(id);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        checkToFindById(id);
        checkToFindById(otherId);

        return userStorage.getCommonFriends(id, otherId);
    }

    private void checkToFindById(Long id) {
        if (userStorage.findById(id).isEmpty()) {
            log.info("Не найдено пользователя с указанным id - {}", id);
            throw new NotFoundException("Пользователь с id - " + id + " не найден");
        }
    }

    private void validateRequestBody(User user) {
        if (user.getId() == null) {
            log.warn("Отсутствует id");
            throw new ValidationException("Отсутствует id");
        }
    }
}
