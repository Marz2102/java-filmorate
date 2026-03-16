package ru.yandex.practicum.filmorate.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.Exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.userDto.UserCreateDto;
import ru.yandex.practicum.filmorate.userDto.UserDto;
import ru.yandex.practicum.filmorate.userDto.UserUpdateDto;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("UserDao") final UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto getUserById(Long id) {
        return userStorage.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + id + " не найден"));
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto addUser(UserCreateDto userCreateDto) {
        log.info("Валидация запроса прошла успешно");
        User user = userStorage.addUser(UserMapper.mapUserCreateDtoToUser(userCreateDto));

        return UserMapper.mapToUserDto(user);
    }

    public UserDto updateUser(UserUpdateDto userUpdateDto) {
        log.info("Валидация запроса прошла успешно");

        User updatedUser = userStorage.findById(userUpdateDto.getId())
                .map(user -> UserMapper.updateUserField(userUpdateDto, user))
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + userUpdateDto.getId() + " не найден"));

        updatedUser = userStorage.updateUser(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    public UserDto addFriend(Long id, Long friendId) {
        checkToFindByIds(id, friendId);
        if (id == friendId) {
            log.info("Ошибка добавления друзей");
            throw new ValidationException("Невозможно добавить себя в друзья");
        }

        User friend = userStorage.addFriend(id, friendId);
        return UserMapper.mapToUserDto(friend);
    }

    public UserDto deleteFriend(Long id, Long friendId) {
        checkToFindByIds(id, friendId);
        if (id == friendId) {
            log.info("Ошибка удаления из друзей");
            throw new ValidationException("Невозможно удалить себя из друзей");
        }

        User friend = userStorage.deleteFriend(id, friendId);
        return UserMapper.mapToUserDto(friend);
    }

    public List<UserDto> getFriends(Long id) {
        checkToFindById(id);

        return userStorage.getFriends(id).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long id, Long otherId) {
        checkToFindByIds(id, otherId);

        return userStorage.getCommonFriends(id, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    private void checkToFindByIds(Long id, Long friendId) {
        if (userStorage.findById(id).isEmpty()) {
            log.info("Не найдено пользователя с указанным id - {}", id);
            throw new NotFoundException("Пользователь с id - " + id + " не найден");
        }

        if (userStorage.findById(friendId).isEmpty()) {
            log.info("Не найдено пользователя с указанным id - {}", friendId);
            throw new NotFoundException("Пользователь с id - " + friendId + " не найден");
        }
    }

    private void checkToFindById(Long id) {
        if (userStorage.findById(id).isEmpty()) {
            log.info("Не найдено пользователя с указанным id - {}", id);
            throw new NotFoundException("Пользователь с id - " + id + " не найден");
        }
    }
}
