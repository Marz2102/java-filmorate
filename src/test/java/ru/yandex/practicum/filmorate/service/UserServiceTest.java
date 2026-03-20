package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserStorage userStorage;

    @Mock
    private FilmStorage filmStorage;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserCreateDto userCreateDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        userCreateDto = new UserCreateDto(
                "test@test.com",
                "test",
                "Test Name",
                LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@test.com");

        verify(userStorage).findById(1L);
    }

    @Test
    void getUserById_NotFound_ShouldThrowException() {
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));

        verify(userStorage).findById(999L);
    }

    @Test
    void getUsers_ShouldReturnList() {
        when(userStorage.getUsers()).thenReturn(List.of(user));

        List<UserDto> result = userService.getUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(userStorage).getUsers();
    }

    @Test
    void addUser_ShouldReturnSavedUser() {
        when(userStorage.addUser(any(User.class))).thenReturn(user);

        UserDto result = userService.addUser(userCreateDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@test.com");

        verify(userStorage).addUser(any(User.class));
    }

    @Test
    void addUser_WithEmptyName_ShouldUseLoginAsName() {
        UserCreateDto dtoWithoutName = new UserCreateDto(
                "test@test.com",
                "testlogin",
                null,
                LocalDate.of(2000, 1, 1)
        );

        User userWithLoginAsName = new User();
        userWithLoginAsName.setId(1L);
        userWithLoginAsName.setEmail("test@test.com");
        userWithLoginAsName.setLogin("testlogin");
        userWithLoginAsName.setName("testlogin");
        userWithLoginAsName.setBirthday(LocalDate.of(2000, 1, 1));

        when(userStorage.addUser(any(User.class))).thenReturn(userWithLoginAsName);

        UserDto result = userService.addUser(dtoWithoutName);

        assertThat(result.getName()).isEqualTo("testlogin");

        verify(userStorage).addUser(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        UserUpdateDto updateDto = new UserUpdateDto(
                1L,
                "updated@test.com",
                "updated",
                "Updated Name",
                LocalDate.of(2000, 1, 1)
        );

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("updated@test.com");
        updatedUser.setLogin("updated");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.updateUser(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(updateDto);

        assertThat(result.getEmail()).isEqualTo("updated@test.com");
        assertThat(result.getLogin()).isEqualTo("updated");
        assertThat(result.getName()).isEqualTo("Updated Name");

        verify(userStorage).findById(1L);
        verify(userStorage).updateUser(any(User.class));
    }

    @Test
    void updateUser_NotFound_ShouldThrowException() {
        UserUpdateDto updateDto = new UserUpdateDto(
                999L,
                "test@test.com",
                "test",
                "Test Name",
                LocalDate.of(2000, 1, 1)
        );

        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(updateDto));

        verify(userStorage).findById(999L);
        verify(userStorage, never()).updateUser(any());
    }

    @Test
    void addFriend_ShouldAddToFriends() {
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.addFriend(1L, 2L)).thenReturn(friend);

        UserDto result = userService.addFriend(1L, 2L);

        assertThat(result).isNotNull();

        verify(userStorage).findById(1L);
        verify(userStorage).findById(2L);
        verify(userStorage).addFriend(1L, 2L);
    }

    @Test
    void addFriend_SameUser_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> userService.addFriend(1L, 1L));

        verify(userStorage, never()).addFriend(anyLong(), anyLong());
    }

    @Test
    void deleteFriend_ShouldRemoveFromFriends() {
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.deleteFriend(1L, 2L)).thenReturn(friend);

        UserDto result = userService.deleteFriend(1L, 2L);

        assertThat(result).isNotNull();

        verify(userStorage).findById(1L);
        verify(userStorage).findById(2L);
        verify(userStorage).deleteFriend(1L, 2L);
    }

    @Test
    void deleteFriend_SameUser_ShouldThrowException() {
        assertThrows(ValidationException.class, () -> userService.deleteFriend(1L, 1L));

        verify(userStorage, never()).deleteFriend(anyLong(), anyLong());
    }

    @Test
    void getFriends_ShouldReturnList() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.getFriends(1L)).thenReturn(List.of(user));

        List<UserDto> result = userService.getFriends(1L);

        assertThat(result).hasSize(1);

        verify(userStorage).findById(1L);
        verify(userStorage).getFriends(1L);
    }

    @Test
    void getCommonFriends_ShouldReturnList() {
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@test.com");
        friend.setLogin("friend");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));

        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.getCommonFriends(1L, 2L)).thenReturn(List.of(friend));

        List<UserDto> result = userService.getCommonFriends(1L, 2L);

        assertThat(result).hasSize(1);

        verify(userStorage).findById(1L);
        verify(userStorage).findById(2L);
        verify(userStorage).getCommonFriends(1L, 2L);
    }

    @Test
    void getRecommendedFilms_ShouldReturnList() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(filmStorage.getRecommendationsByUserId(1L)).thenReturn(List.of());

        userService.getRecommendedFilms(1L);

        verify(userStorage).findById(1L);
        verify(filmStorage).getRecommendationsByUserId(1L);
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userStorage).deleteUser(1L);

        userService.deleteUser(1L);

        verify(userStorage).findById(1L);
        verify(userStorage).deleteUser(1L);
    }

    @Test
    void deleteUser_NotFound_ShouldThrowException() {
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(999L));

        verify(userStorage).findById(999L);
        verify(userStorage, never()).deleteUser(anyLong());
    }
}
