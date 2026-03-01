package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryUserStorage implements UserStorage {

    @Override
    public Optional<User> findById(Long id) {
        return Optional.of(users.get(id));
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        user.setId(generateNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        User user = users.get(id);
        User friend = users.get(friendId);

        user.addFriend(friendId);
        friend.addFriend(id);

        return friend;
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        user.deleteFriend(friendId);
        friend.deleteFriend(userId);

        return friend;
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = users.get(userId);
        return user.getFriends()
                .stream()
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        User user1 = users.get(userId1);
        User user2 = users.get(userId2);
        return user1.getFriends()
                .stream()
                .filter(user2.getFriends()::contains)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public Long generateNextId() {
        long currentId = users
                .keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);

        return currentId + 1;
    }
}
