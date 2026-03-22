package ru.yandex.practicum.filmorate.fixtures;

import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static UserCreateDto createDefaultUserCreateDto() {
        return new UserCreateDto(
                "test@test.com",
                "test",
                "Test User",
                LocalDate.of(2000, 1, 1)
        );
    }

    public static User createDefaultUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    public static FilmCreateDto createDefaultFilmCreateDto() {
        return new FilmCreateDto(
                "Test Film",
                "Test Description",
                LocalDate.of(2020, 1, 1),
                120,
                null,
                null,
                null
        );
    }

    public static Film createDefaultFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        return film;
    }

    public static UserCreateDto createUserWithEmptyName() {
        return new UserCreateDto(
                "empty@test.com",
                "empty",
                "",
                LocalDate.of(2000, 1, 1)
        );
    }

    public static FilmCreateDto createFilmWithInvalidDate() {
        return new FilmCreateDto(
                "Old Film",
                "Description",
                LocalDate.of(1890, 1, 1),
                100,
                null,
                null,
                null
        );
    }
}