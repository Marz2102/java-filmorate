package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getUsers() {
        log.info("Получен список всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        user.setId(generateNextId());
        users.put(user.getId(), user);

        log.info("Успешно добавлен новый пользователь с id = {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);

        log.info("Данные пользователя с id = {} успешно обновлены", user.getId());
        return user;
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        User user = users.get(id);
        User friend = users.get(friendId);

        user.addFriend(friendId);
        friend.addFriend(id);

        log.info("Пользователи с id - {} и {} теперь друзья", id, friendId);
        return friend;
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {
        User user = users.get(id);
        User friend = users.get(friendId);

        user.deleteFriend(friendId);
        friend.deleteFriend(id);

        log.info("Пользователи с id - {} и {} больше не друзья", id, friendId);
        return friend;
    }

    @Override
    public List<User> getFriends(Long id) {
        User user = users.get(id);

        log.info("Получен список всех друзей пользователя с id = {}", id);
        return user.getFriends()
                .stream()
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        User user = users.get(id);
        User otherUser = users.get(otherId);

        log.info("Получен список общих друзей пользователей с id - {} и {}", id, otherId);
        return user.getFriends()
                .stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public List<User> clear() {
        List<User> allUsers = new ArrayList<>(users.values());
        users.clear();
        return allUsers;
    }

    @Override
    public Long generateNextId() {
        long currentId = users
                .keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L) + 1;

        log.debug("Сгенерирован новый id - {}", currentId);
        return currentId;
    }
}
