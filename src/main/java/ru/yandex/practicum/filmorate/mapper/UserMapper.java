package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dto.user.FriendDto;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateDto;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static User mapUserCreateDtoToUser(UserCreateDto userCreateDto) {
        User user = new User();

        user.setEmail(userCreateDto.getEmail());
        if (userCreateDto.getName() == null || userCreateDto.getName().isEmpty()) {
            user.setName(userCreateDto.getLogin());
            log.debug("Имя пусто, используется логин - {}", user.getName());
        } else {
            user.setName(userCreateDto.getName());
        }

        user.setLogin(userCreateDto.getLogin());
        user.setBirthday(userCreateDto.getBirthday());

        return user;
    }

    public static UserDto mapUserToUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        userDto.setLogin(user.getLogin());
        userDto.setBirthday(user.getBirthday());

        if (user.getFriends() != null) {
            Map<FriendDto, FriendshipStatus> friends = user.getFriends().entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        f -> mapToFriendDto(f.getKey()),
                        Map.Entry::getValue
                    ));

            userDto.setFriends(friends);
        }

        return userDto;
    }

    public static FriendDto mapToFriendDto(User user) {
        FriendDto friendDto = new FriendDto();
        friendDto.setId(user.getId());
        friendDto.setEmail(user.getEmail());
        friendDto.setName(user.getName());
        friendDto.setLogin(user.getLogin());
        friendDto.setBirthday(user.getBirthday());

        return friendDto;
    }

    public static User updateUserField(UserUpdateDto userUpdateDto, User user) {
        if (userUpdateDto.getName() != null && !userUpdateDto.getName().isEmpty()) {
            user.setName(userUpdateDto.getName());
            log.debug("Обновили имя пользователя - {}", user.getName());
        }

        if (userUpdateDto.getBirthday() != null) {
            user.setBirthday(userUpdateDto.getBirthday());
            log.debug("Обновили дату рождения пользователя - {}", user.getBirthday());
        }

        if (userUpdateDto.getEmail() != null) {
            user.setEmail(userUpdateDto.getEmail());
            log.debug("Обновили почту пользователя - {}", user.getEmail());
        }

        if (userUpdateDto.getLogin() != null) {
            user.setLogin(userUpdateDto.getLogin());
            log.debug("Обновили логин пользователя - {}", user.getLogin());
        }

        return user;
    }
}
