package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventStorage eventStorage;

    public UserService(@Qualifier("UserDao") final UserStorage userStorage,
                       @Qualifier("FilmDao") final FilmStorage filmStorage,
                       @Qualifier("EventDao") final EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventStorage = eventStorage;
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

    public List<FilmDto> getRecommendedFilms(Long id) {
        checkToFindById(id);

        return filmStorage.getRecommendationsByUserId(id)
                .stream()
                .map(FilmMapper::mapFilmToFilmDto)
                .toList();
    }

    public void deleteUser(Long userId) {
        log.info("Вызван метод удаления пользователя с id {}", userId);

        getUserById(userId);

        userStorage.deleteUser(userId);
        log.info("Пользователь с id {} успешно удален", userId);
    }

    public List<EventDto> getFeed(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + userId + " не найден"));

        Set<Long> usersId = user.getFriends().keySet().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        usersId.add(userId);

        List<Event> events = new ArrayList<>();
        usersId.stream()
                .map(eventStorage::getEvents)
                .forEach(events::addAll);

        return events.stream()
                .map(EventMapper::mapEventToEventDto)
                .sorted(Comparator.comparing(EventDto::getTimestamp))//.reversed())
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
