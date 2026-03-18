package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Transactional
@Repository("UserDao")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    public UserDbStorage(DataSource dataSource, UserRowMapper userRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.userRowMapper = userRowMapper;
    }

    @Override
    public Optional<User> findById(Long id) {
        String query = "SELECT id, email, name, login, birthday_date FROM users WHERE id = ?";
        try {
            User user = jdbc.queryForObject(query, userRowMapper, id);

            if (user != null) {
                user.setFriends(getFriendsForUserId(user.getId()));
            }

            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getUsers() {
        String query = "SELECT id, email, name, login, birthday_date FROM users";

        List<User> users = jdbc.query(query, userRowMapper);

        Map<Long, Map<User, FriendshipStatus>> allFriends = getFriendsForAllUsers();
        users.forEach(user -> user.setFriends(allFriends.getOrDefault(user.getId(), Collections.emptyMap())));

        return users;
    }

    @Override
    public User addUser(User user) {
        String query = "INSERT INTO users (email, name, login, birthday_date) VALUES (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getName());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = (Long) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        if (id != null) {
            user.setId(id);
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось добавить данные");
        }

        return user;
    }

    @Override
    public User updateUser(User user) {
        String query = "UPDATE users SET email = ?, name = ?, login = ?, birthday_date = ? WHERE id = ?";
        int rowsUpdated = jdbc.update(query,
                user.getEmail(),
                user.getName(),
                user.getLogin(),
                user.getBirthday(),
                user.getId());

        if (rowsUpdated == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обновить данные");
        }

        user.setFriends(getFriendsForUserId(user.getId()));

        return user;
    }

    @Override
    public User addFriend(Long userId, Long friendId) {
        String queryForReverseLink = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        String queryForDuplicate = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";

        Long countReverseLink = jdbc.queryForObject(queryForReverseLink, Long.class, friendId, userId);
        Long countDuplicate = jdbc.queryForObject(queryForDuplicate, Long.class, userId, friendId);

        if (countReverseLink == null || countDuplicate == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при выполнении запроса");
        }

        if (countDuplicate > 0) {
            return findById(userId).orElse(null);
        }

        if (countReverseLink > 0) {
            String queryUpdate = "UPDATE friends SET is_confirmed = TRUE WHERE user_id = ? AND friend_id = ?";
            String queryInsert = "INSERT INTO friends (user_id, friend_id, is_confirmed) VALUES (?, ?, TRUE)";

            int rowsUpdated = jdbc.update(queryUpdate, friendId, userId);

            if (rowsUpdated == 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обновить данные");
            }

            jdbc.update(queryInsert, userId, friendId);

        } else {
            String query = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
            jdbc.update(query, userId, friendId);
        }

        return findById(friendId).orElse(null);
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) {
        String queryForReverseLink = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        String queryForDuplicate = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";

        Long countReverseLink = jdbc.queryForObject(queryForReverseLink, Long.class, friendId, userId);
        Long countDuplicate = jdbc.queryForObject(queryForDuplicate, Long.class, userId, friendId);

        if (countReverseLink == null || countDuplicate == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при выполнении запроса");
        }

        if (countDuplicate == 0) {
            return findById(userId).orElse(null);
        }

        String queryDelete = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbc.update(queryDelete, userId, friendId);

        if (countReverseLink > 0) {
            String queryUpdate = "UPDATE friends SET is_confirmed = FALSE WHERE user_id = ? AND friend_id = ?";
            int rowsUpdated = jdbc.update(queryUpdate, friendId, userId);

            if (rowsUpdated == 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось обновить данные");
            }
        }

        return findById(friendId).orElse(null);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String query = """
                SELECT u.id, u.email, u.name, u.login, u.birthday_date
                FROM friends as f
                INNER JOIN users as u ON f.friend_id = u.id
                WHERE f.user_id = ?
                """;

        List<User> users = jdbc.query(query, userRowMapper, userId);

        Map<Long, Map<User, FriendshipStatus>> allFriends = getFriendsForAllUsers();
        users.forEach(user -> user.setFriends(allFriends.getOrDefault(user.getId(), Collections.emptyMap())));

        return users;
    }

    @Override
    public List<User> getCommonFriends(Long userId1, Long userId2) {
        String query = """
                SELECT u.id, u.email, u.name, u.login, u.birthday_date
                FROM users as u
                INNER JOIN friends as f1 ON u.id = f1.friend_id
                                         AND f1.user_id = ?
                INNER JOIN friends as f2 ON u.id = f2.friend_id
                                         AND f2.user_id = ?
                """;

        List<User> users = jdbc.query(query, userRowMapper, userId1, userId2);

        Map<Long, Map<User, FriendshipStatus>> allFriends = getFriendsForAllUsers();
        users.forEach(user -> user.setFriends(allFriends.getOrDefault(user.getId(), Collections.emptyMap())));

        return users;
    }

    private Map<Long, Map<User, FriendshipStatus>> getFriendsForAllUsers() {
        String query = """
                SELECT u.id, u1.id as friend_id, u1.email as friend_email, u1.name as friend_name,
                       u1.login as friend_login, u1.birthday_date as friend_birthday_date, f.is_confirmed
                FROM users as u
                INNER JOIN friends as f ON u.id = f.user_id
                INNER JOIN users as u1 ON u1.id = f.friend_id
                """;

        Map<Long, Map<User, FriendshipStatus>> allFriends = new HashMap<>();

        jdbc.query(query, (rs) -> {
            Long userId = rs.getLong("id");
            Map<User, FriendshipStatus> friends = allFriends.computeIfAbsent(userId, k -> new HashMap<>());

            User friend = new User();
            friend.setId(rs.getLong("friend_id"));
            friend.setEmail(rs.getString("friend_email"));
            friend.setName(rs.getString("friend_name"));
            friend.setLogin(rs.getString("friend_login"));
            friend.setBirthday(rs.getDate("friend_birthday_date").toLocalDate());

            friends.put(friend, rs.getBoolean("is_confirmed") ? FriendshipStatus.CONFIRMED : FriendshipStatus.NOT_CONFIRMED);
        });

        return allFriends;
    }

    private Map<User, FriendshipStatus> getFriendsForUserId(Long id) {
        String query = """
                SELECT u.id, u.email, u.name, u.login, u.birthday_date, f.is_confirmed
                FROM users as u
                INNER JOIN friends as f ON u.id = f.friend_id AND f.user_id = ?
                """;

        Map<User, FriendshipStatus> friends = new HashMap<>();

        jdbc.query(query, (rs) -> {
            User friend = new User();

            friend.setId(rs.getLong("id"));
            friend.setEmail(rs.getString("email"));
            friend.setName(rs.getString("name"));
            friend.setLogin(rs.getString("login"));
            friend.setBirthday(rs.getDate("birthday_date").toLocalDate());

            friends.put(friend, rs.getBoolean("is_confirmed") ? FriendshipStatus.CONFIRMED : FriendshipStatus.NOT_CONFIRMED);
        }, id);

        return friends;
    }
}
