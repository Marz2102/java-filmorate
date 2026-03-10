package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository("DAO")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    public UserDbStorage(DataSource dataSource, UserRowMapper userRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.userRowMapper = userRowMapper;
    };

    @Override
    public Optional<User> findById(Long id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbc.queryForObject(query, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getUsers() {
        String query = "SELECT * FROM users";
        return jdbc.query(query, userRowMapper);
    }

    @Override
    public User addUser(User user) {
        String query = "INSERT INTO users (email, name, login, birthday) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getName());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        if (id != null) {
            user.setId(id);
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }

        return user;
    }

    @Override
    public User updateUser(User user) {
        String query = "UPDATE users SET email = ?, name = ?, login = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbc.update(query,
                user.getEmail(),
                user.getName(),
                user.getLogin(),
                user.getBirthday(),
                user.getId());

        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
        return user;
    }

    @Override
    public User addFriend(Long userId, Long friendId) {
        String query = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbc.update(query, userId, friendId);
        jdbc.update(query, friendId, userId);

        User friend = findById(friendId).get();
        friend.addFriend(userId);
        return friend;
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) {
        String query = "DELETE FROM friends WHERE user_id = ? and friend_id = ?";
        jdbc.update(query, userId, friendId);
        jdbc.update(query, friendId, userId);

        User friend = findById(friendId).get();
        friend.addFriend(userId);
        return friend;
    }

    @Override
    public List<User> getFriends(Long userId) {
        String query = """
                SELECT u.*
                FROM friends as f
                INNER JOIN users as u ON f.friend_id = u.id
                WHERE f.user_id = ?
                """;
        return jdbc.query(query, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        String query = """
                SELECT u.*
                FROM users as u
                INNER JOIN friends as f1 ON u.id = f1.friend_id
                                         AND f1.user_id = ?
                INNER JOIN friends as f2 ON u.id = f2.friend_id
                                         AND f2.user_id = ?
                """;
        return jdbc.query(query, userRowMapper, userId1, userId2);
    }

}
