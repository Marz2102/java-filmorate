package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateApplicationTests {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    FilmorateApplicationTests(@Qualifier("UserDao") UserStorage userStorage, @Qualifier("FilmDao") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("TestName");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Film film = new Film();
        film.setId(1L);
        film.setName("TestFilm");
        film.setDescription("TestDescription");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(1990, 12, 12));
        film.setGenres(new HashSet<>(List.of(new Genre(1L, "Комедия"),
                new Genre(3L, "Мультфильм"))));
        film.setMpa(new Rating(1L, "G"));

        userStorage.addUser(user);
        filmStorage.addFilm(film);
    }

    @Test
    public void testFindUserAndFilmById() {
        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );

        Optional<Film> filmOptional = filmStorage.findById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindAllUsersAndFilms() {
        User user = new User();
        user.setId(2L);
        user.setEmail("Test@test2.com");
        user.setLogin("test2");
        user.setName("TestName2");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Film film = new Film();
        film.setId(2L);
        film.setName("TestFilm2");
        film.setDescription("TestDescription2");
        film.setDuration(150);
        film.setReleaseDate(LocalDate.of(1980, 12, 12));

        List<User> users = userStorage.getUsers();
        List<Film> films = filmStorage.getFilms();

        assertEquals(1, users.size());
        assertEquals(1, films.size());

        userStorage.addUser(user);
        filmStorage.addFilm(film);

        users = userStorage.getUsers();
        films = filmStorage.getFilms();

        assertEquals(2, users.size());
        assertEquals(2, films.size());
    }

    @Test
    public void testUpdateUserAndFilm() {
        User user = new User();
        user.setId(1L);
        user.setEmail("UpdateTest@test.com");
        user.setLogin("test");
        user.setName("TestName");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Film film = new Film();
        film.setId(1L);
        film.setName("UpdateTestFilm");
        film.setDescription("TestDescription");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(1990, 12, 12));

        user = userStorage.addUser(user);
        film = filmStorage.addFilm(film);

        assertEquals("UpdateTest@test.com", user.getEmail());
        assertEquals("UpdateTestFilm", film.getName());
    }

    @Test
    public void testAddAndDeleteFriend() {
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("test2@test.com");
        friend.setLogin("test2");
        friend.setName("TestName2");
        friend.setBirthday(LocalDate.of(2000, 1, 1));

        userStorage.addUser(friend);
        userStorage.addFriend(1L, 2L);

        assertEquals(1, userStorage.getFriends(1L).size());
        assertEquals(0, userStorage.getFriends(2L).size());

        userStorage.deleteFriend(1L, 2L);

        assertEquals(0, userStorage.getFriends(1L).size());
        assertEquals(0, userStorage.getFriends(2L).size());
    }

    @Test
    public void testGetCommonFriends() {
        User friend1 = new User();
        friend1.setId(2L);
        friend1.setEmail("test2@test.com");
        friend1.setLogin("test2");
        friend1.setName("TestName2");
        friend1.setBirthday(LocalDate.of(2000, 1, 1));

        User friend2 = new User();
        friend2.setId(3L);
        friend2.setEmail("test3@test.com");
        friend2.setLogin("test3");
        friend2.setName("TestName3");
        friend2.setBirthday(LocalDate.of(2000, 1, 1));

        userStorage.addUser(friend1);
        userStorage.addUser(friend2);

        assertEquals(0, userStorage.getCommonFriends(1L, 3L).size());

        userStorage.addFriend(1L, 2L);
        userStorage.addFriend(3L, 2L);

        assertEquals(1, userStorage.getCommonFriends(1L, 3L).size());

        userStorage.deleteFriend(1L, 2L);

        assertEquals(0, userStorage.getCommonFriends(1L, 3L).size());
    }

    @Test
    public void testGetMostLikedFilms() {
        User user = new User();
        user.setId(2L);
        user.setEmail("Test@test2.com");
        user.setLogin("test2");
        user.setName("TestName2");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        Film film = new Film();
        film.setId(2L);
        film.setName("TestFilm2");
        film.setDescription("TestDescription2");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(1990, 12, 12));

        user = userStorage.addUser(user);
        film = filmStorage.addFilm(film);

        System.out.println(filmStorage.getFilms());

        assertEquals(2, filmStorage.getMostLikedFilms(100).size());

        filmStorage.addLike(1L, 1L);

        List<Film> films = filmStorage.getMostLikedFilms(1);
        assertEquals(1L, films.getFirst().getId());

        filmStorage.deleteLike(1L, 1L);
        filmStorage.addLike(2L, 2L);

        films = filmStorage.getMostLikedFilms(1);
        assertEquals(2L, films.getFirst().getId());

    }

    @Test
    public void testGetGenres() {
        assertEquals(6, filmStorage.getGenres().size());
        assertEquals("Документальный", filmStorage.findGenreById(5L).get().getName());
    }

    @Test
    public void testGetRatings() {
        assertEquals(5, filmStorage.getRatings().size());
        assertEquals("PG-13", filmStorage.findRatingById(3L).get().getName());
    }
}

