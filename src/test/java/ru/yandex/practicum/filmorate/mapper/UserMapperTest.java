package ru.yandex.practicum.filmorate.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserDto;
import ru.yandex.practicum.filmorate.dto.user.UserUpdateDto;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void mapUserCreateDtoToUser_WithName_ShouldUseProvidedName() {
        UserCreateDto dto = new UserCreateDto(
                "test@test.com",
                "login",
                "Provided Name",
                LocalDate.of(2000, 1, 1)
        );

        User user = UserMapper.mapUserCreateDtoToUser(dto);

        assertThat(user.getName()).isEqualTo("Provided Name");
        assertThat(user.getLogin()).isEqualTo("login");
        assertThat(user.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void mapUserCreateDtoToUser_WithEmptyName_ShouldUseLogin() {
        UserCreateDto dto = new UserCreateDto(
                "test@test.com",
                "login",
                "",
                LocalDate.of(2000, 1, 1)
        );

        User user = UserMapper.mapUserCreateDtoToUser(dto);

        assertThat(user.getName()).isEqualTo("login");
    }

    @Test
    void mapUserCreateDtoToUser_WithNullName_ShouldUseLogin() {
        UserCreateDto dto = new UserCreateDto(
                "test@test.com",
                "login",
                null,
                LocalDate.of(2000, 1, 1)
        );

        User user = UserMapper.mapUserCreateDtoToUser(dto);

        assertThat(user.getName()).isEqualTo("login");
    }

    @Test
    void mapUserToUserDto_ShouldMapCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        UserDto dto = UserMapper.mapUserToUserDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("test@test.com");
        assertThat(dto.getLogin()).isEqualTo("login");
        assertThat(dto.getName()).isEqualTo("Name");
        assertThat(dto.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    void updateUserField_ShouldUpdateNonNullFields() {
        User user = new User();
        user.setId(1L);
        user.setEmail("old@test.com");
        user.setLogin("old");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        UserUpdateDto updateDto = new UserUpdateDto(
                1L,
                "new@test.com",
                "new",
                "New Name",
                LocalDate.of(2000, 1, 1)
        );

        User updated = UserMapper.updateUserField(updateDto, user);

        assertThat(updated.getEmail()).isEqualTo("new@test.com");
        assertThat(updated.getLogin()).isEqualTo("new");
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    void updateUserField_WithNullFields_ShouldKeepOldValues() {
        User user = new User();
        user.setId(1L);
        user.setEmail("old@test.com");
        user.setLogin("old");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        UserUpdateDto updateDto = new UserUpdateDto(
                1L,
                null,
                null,
                null,
                null
        );

        User updated = UserMapper.updateUserField(updateDto, user);

        assertThat(updated.getEmail()).isEqualTo("old@test.com");
        assertThat(updated.getLogin()).isEqualTo("old");
        assertThat(updated.getName()).isEqualTo("Old Name");
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }
}
