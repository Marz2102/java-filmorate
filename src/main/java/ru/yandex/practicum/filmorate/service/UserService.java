package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateDto;

import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("UserDao") final UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto getUserById(Long id) {
        return userStorage.findById(id)
                .map(UserMapper::mapUserToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + id + " не найден"));
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(UserMapper::mapUserToUserDto)
                .toList();
    }

    public UserDto addUser(UserCreateDto userCreateDto) {
        log.info("Валидация запроса прошла успешно");
        User user = userStorage.addUser(UserMapper.mapUserCreateDtoToUser(userCreateDto));

        return UserMapper.mapUserToUserDto(user);
    }

    public UserDto updateUser(UserUpdateDto userUpdateDto) {
        log.info("Валидация запроса прошла успешно");

        User updatedUser = userStorage.findById(userUpdateDto.getId())
                .map(user -> UserMapper.updateUserField(userUpdateDto, user))
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + userUpdateDto.getId() + " не найден"));

        updatedUser = userStorage.updateUser(updatedUser);
        return UserMapper.mapUserToUserDto(updatedUser);
    }

    public UserDto addFriend(Long id, Long friendId) {
        checkToFindByIds(id, friendId);
        if (id == friendId) {
            log.info("Ошибка добавления друзей");
            throw new ValidationException("Невозможно добавить себя в друзья");
        }

        User friend = userStorage.addFriend(id, friendId);
        return UserMapper.mapUserToUserDto(friend);
    }

    public UserDto deleteFriend(Long id, Long friendId) {
        checkToFindByIds(id, friendId);
        if (id == friendId) {
            log.info("Ошибка удаления из друзей");
            throw new ValidationException("Невозможно удалить себя из друзей");
        }

        User friend = userStorage.deleteFriend(id, friendId);
        return UserMapper.mapUserToUserDto(friend);
    }

    public List<UserDto> getFriends(Long id) {
        checkToFindById(id);

        return userStorage.getFriends(id).stream()
                .map(UserMapper::mapUserToUserDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long id, Long otherId) {
        checkToFindByIds(id, otherId);

        return userStorage.getCommonFriends(id, otherId).stream()
                .map(UserMapper::mapUserToUserDto)
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
