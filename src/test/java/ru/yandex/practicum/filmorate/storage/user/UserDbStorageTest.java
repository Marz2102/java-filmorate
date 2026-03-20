package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDbStorageTest {

    @Autowired
    @Qualifier("UserDao")
    private UserStorage userStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setLogin("test");
        testUser.setName("TestName");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.addUser(testUser);
    }

    @Test
    void findById_ShouldReturnUser() {
        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    void getUsers_ShouldReturnAllUsers() {
        List<User> users = userStorage.getUsers();
        assertEquals(1, users.size());

        User newUser = new User();
        newUser.setEmail("test2@test.com");
        newUser.setLogin("test2");
        newUser.setName("TestName2");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.addUser(newUser);

        users = userStorage.getUsers();
        assertEquals(2, users.size());
    }

    @Test
    void addUser_ShouldGenerateId() {
        User newUser = new User();
        newUser.setEmail("new@test.com");
        newUser.setLogin("new");
        newUser.setName("NewName");
        newUser.setBirthday(LocalDate.of(1995, 5, 5));

        User saved = userStorage.addUser(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        testUser.setEmail("updated@test.com");
        testUser.setName("UpdatedName");

        User updated = userStorage.updateUser(testUser);

        assertEquals("updated@test.com", updated.getEmail());
        assertEquals("UpdatedName", updated.getName());

        Optional<User> fromDb = userStorage.findById(testUser.getId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getEmail()).isEqualTo("updated@test.com");
                    assertThat(user.getName()).isEqualTo("UpdatedName");
                });
    }

    @Test
    void addFriend_ShouldAddToFriendsList() {
        User friend = new User();
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.addUser(friend);

        userStorage.addFriend(1L, 2L);

        assertEquals(1, userStorage.getFriends(1L).size());
        assertEquals(0, userStorage.getFriends(2L).size());
    }

    @Test
    void deleteFriend_ShouldRemoveFromFriendsList() {
        User friend = new User();
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.addUser(friend);

        userStorage.addFriend(1L, 2L);
        userStorage.deleteFriend(1L, 2L);

        assertEquals(0, userStorage.getFriends(1L).size());
        assertEquals(0, userStorage.getFriends(2L).size());
    }

    @Test
    void getFriends_ShouldReturnListOfFriends() {
        User friend1 = new User();
        friend1.setEmail("friend1@test.com");
        friend1.setLogin("friend1");
        friend1.setName("Friend1");
        friend1.setBirthday(LocalDate.of(2000, 1, 1));

        User friend2 = new User();
        friend2.setEmail("friend2@test.com");
        friend2.setLogin("friend2");
        friend2.setName("Friend2");
        friend2.setBirthday(LocalDate.of(2000, 1, 1));

        userStorage.addUser(friend1);
        userStorage.addUser(friend2);

        userStorage.addFriend(1L, 2L);
        userStorage.addFriend(1L, 3L);

        List<User> friends = userStorage.getFriends(1L);
        assertEquals(2, friends.size());
    }

    @Test
    void getCommonFriends_ShouldReturnMutualFriends() {
        User friend1 = new User();
        friend1.setEmail("friend1@test.com");
        friend1.setLogin("friend1");
        friend1.setName("Friend1");
        friend1.setBirthday(LocalDate.of(2000, 1, 1));

        User friend2 = new User();
        friend2.setEmail("friend2@test.com");
        friend2.setLogin("friend2");
        friend2.setName("Friend2");
        friend2.setBirthday(LocalDate.of(2000, 1, 1));

        User otherUser = new User();
        otherUser.setEmail("other@test.com");
        otherUser.setLogin("other");
        otherUser.setName("Other");
        otherUser.setBirthday(LocalDate.of(2000, 1, 1));

        userStorage.addUser(friend1);
        userStorage.addUser(friend2);
        userStorage.addUser(otherUser);

        userStorage.addFriend(1L, 2L);
        userStorage.addFriend(3L, 2L);

        List<User> commonFriends = userStorage.getCommonFriends(1L, 3L);
        assertEquals(1, commonFriends.size());
        assertEquals(friend1.getId(), commonFriends.get(0).getId());
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        User userToDelete = new User();
        userToDelete.setEmail("delete@test.com");
        userToDelete.setLogin("delete");
        userToDelete.setName("To Delete");
        userToDelete.setBirthday(LocalDate.of(2000, 1, 1));
        userToDelete = userStorage.addUser(userToDelete);

        Long userId = userToDelete.getId();

        Optional<User> foundUser = userStorage.findById(userId);
        assertThat(foundUser).isPresent();

        userStorage.deleteUser(userId);

        Optional<User> deletedUser = userStorage.findById(userId);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void deleteNonExistentUser_ShouldThrowException() {
        assertThrows(ResponseStatusException.class, () -> {
            userStorage.deleteUser(999L);
        });
    }
}