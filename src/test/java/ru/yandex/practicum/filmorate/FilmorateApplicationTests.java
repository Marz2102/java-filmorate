package ru.yandex.practicum.filmorate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateApplicationTests {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;
    private final ReviewStorage reviewStorage;

    @Autowired
    FilmorateApplicationTests(
            @Qualifier("UserDao") final UserStorage userStorage,
            @Qualifier("FilmDao") final FilmStorage filmStorage,
            @Qualifier("GenreDao") final GenreStorage genreStorage,
            @Qualifier("MpaDao") final MpaStorage mpaStorage,
            @Qualifier("DirectorDao") final DirectorStorage directorStorage,
            @Qualifier("ReviewDao") final ReviewStorage reviewStorage
    ) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
        this.reviewStorage = reviewStorage;
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
        film.setMpa(new Mpa(1L, "G"));

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

        userStorage.addUser(user);
        filmStorage.addFilm(film);

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
        assertEquals(6, genreStorage.getGenres().size());
        assertEquals("Документальный", genreStorage.findById(5L).get().getName());
    }

    @Test
    public void testGetAllMpa() {
        assertEquals(5, mpaStorage.getAllMpa().size());
        assertEquals("PG-13", mpaStorage.findById(3L).get().getName());
    }

    @Test
    public void testAddAndFindDirector() {
        Director director = new Director();
        director.setName("Director One");

        Director saved = directorStorage.addDirector(director);
        assertThat(saved.getId()).isNotNull();

        Optional<Director> fromDb = directorStorage.findById(saved.getId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(d ->
                        assertThat(d).hasFieldOrPropertyWithValue("name", "Director One")
                );
    }

    @Test
    public void testUpdateDirector() {
        Director director = new Director();
        director.setName("Old Name");
        Director saved = directorStorage.addDirector(director);

        saved.setName("New Name");
        Director updated = directorStorage.updateDirector(saved);

        Optional<Director> fromDb = directorStorage.findById(updated.getId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(d ->
                        assertThat(d).hasFieldOrPropertyWithValue("name", "New Name")
                );
    }

    @Test
    public void testDeleteDirector() {
        Director director = new Director();
        director.setName("To Delete");
        Director saved = directorStorage.addDirector(director);

        directorStorage.deleteDirector(saved.getId());

        Optional<Director> fromDb = directorStorage.findById(saved.getId());
        assertThat(fromDb).isNotPresent();
    }

    @Test
    public void testGetFilmsByDirectorSortedByLikes() {
        Director director = new Director();
        director.setName("Likes Director");
        Director savedDirector = directorStorage.addDirector(director);

        Film film1 = new Film();
        film1.setName("Film1");
        film1.setDescription("desc1");
        film1.setDuration(100);
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setMpa(new Mpa(1L, "G"));
        film1.setDirectors(new HashSet<>(List.of(savedDirector)));
        film1 = filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film2");
        film2.setDescription("desc2");
        film2.setDuration(110);
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        film2.setDirectors(new HashSet<>(List.of(savedDirector)));
        film2 = filmStorage.addFilm(film2);

        User u1 = new User();
        u1.setEmail("u1@test.com");
        u1.setLogin("u1");
        u1.setName("u1");
        u1.setBirthday(LocalDate.of(2000, 1, 1));
        u1 = userStorage.addUser(u1);

        User u2 = new User();
        u2.setEmail("u2@test.com");
        u2.setLogin("u2");
        u2.setName("u2");
        u2.setBirthday(LocalDate.of(2000, 1, 1));
        u2 = userStorage.addUser(u2);

        filmStorage.addLike(film1.getId(), u1.getId());
        filmStorage.addLike(film2.getId(), u1.getId());
        filmStorage.addLike(film2.getId(), u2.getId());

        List<Film> films = filmStorage.getFilmsByDirectorId(savedDirector.getId(), "likes");

        assertEquals(2, films.size());
        assertEquals(film2.getId(), films.get(0).getId()); // больше лайков
        assertEquals(film1.getId(), films.get(1).getId());
    }

    @Test
    public void testGetFilmsByDirectorSortedByYear() {
        Director director = new Director();
        director.setName("Year Director");
        Director savedDirector = directorStorage.addDirector(director);

        Film oldFilm = new Film();
        oldFilm.setName("Old");
        oldFilm.setDescription("old");
        oldFilm.setDuration(90);
        oldFilm.setReleaseDate(LocalDate.of(1990, 1, 1));
        oldFilm.setMpa(new Mpa(1L, "G"));
        oldFilm.setDirectors(new HashSet<>(List.of(savedDirector)));
        oldFilm = filmStorage.addFilm(oldFilm);

        Film newFilm = new Film();
        newFilm.setName("New");
        newFilm.setDescription("new");
        newFilm.setDuration(100);
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setMpa(new Mpa(1L, "G"));
        newFilm.setDirectors(new HashSet<>(List.of(savedDirector)));
        newFilm = filmStorage.addFilm(newFilm);

        List<Film> films = filmStorage.getFilmsByDirectorId(savedDirector.getId(), "year");

        assertEquals(2, films.size());
        assertEquals(oldFilm.getId(), films.get(0).getId()); // раньше дата
        assertEquals(newFilm.getId(), films.get(1).getId());
    }

    @Test
    public void testDeleteUser() {
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
    public void testDeleteNonExistentUser() {
        Long nonExistentId = 999L;

        assertThrows(ResponseStatusException.class, () -> {
            userStorage.deleteUser(nonExistentId);
        });
    }

    @Test
    public void testDeleteFilm() {
        Film filmToDelete = new Film();
        filmToDelete.setName("Film To Delete");
        filmToDelete.setDescription("Will be deleted");
        filmToDelete.setDuration(100);
        filmToDelete.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmToDelete.setMpa(new Mpa(1L, "G"));
        filmToDelete = filmStorage.addFilm(filmToDelete);

        Long filmId = filmToDelete.getId();

        Optional<Film> foundFilm = filmStorage.findById(filmId);
        assertThat(foundFilm).isPresent();

        filmStorage.deleteFilm(filmId);

        Optional<Film> deletedFilm = filmStorage.findById(filmId);
        assertThat(deletedFilm).isNotPresent();
    }

    @Test
    public void testDeleteNonExistentFilm() {
        Long nonExistentId = 999L;

        assertThrows(ResponseStatusException.class, () -> {
            filmStorage.deleteFilm(nonExistentId);
        });
    }


    @Test
    public void testGetCommonFilms() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("test@test2.com");
        newUser.setLogin("test2");
        newUser.setName("TestName2");
        newUser.setBirthday(LocalDate.of(2000, 10, 10));
        userStorage.addUser(newUser);

        Film newFilm = new Film();
        newFilm.setId(2L);
        newFilm.setName("TestFilm2");
        newFilm.setDescription("TestDescription2");
        newFilm.setDuration(200);
        newFilm.setReleaseDate(LocalDate.of(1990, 12, 12));
        newFilm.setGenres(new HashSet<>(List.of(new Genre(5L, "Документальный"))));
        newFilm.setMpa(new Mpa(5L, "NC-17"));
        filmStorage.addFilm(newFilm);

        List<Film> films = filmStorage.getCommonFilms(1L, 2L);
        assertEquals(0, films.size());

        filmStorage.addLike(1L, 1L);
        filmStorage.addLike(2L, 2L);

        films = filmStorage.getCommonFilms(1L, 2L);
        assertEquals(0, films.size());

        filmStorage.addLike(1L, 2L);

        films = filmStorage.getCommonFilms(1L, 2L);
        assertEquals(1, films.size());

        filmStorage.deleteLike(1L, 2L);

        films = filmStorage.getCommonFilms(1L, 2L);
        assertEquals(0, films.size());
    }

    @Test
    public void testGetRecommendedFilms() {
// User 1 (G)
        User user1 = new User();
        user1.setLogin("G");
        user1.setEmail("test1@email.com");
        user1.setName("123");
        user1.setBirthday(LocalDate.of(2000, 2, 2));
        user1 = userStorage.addUser(user1);

// User 2 (test2)
        User user2 = new User();
        user2.setLogin("test2");
        user2.setEmail("test2@email.com");
        user2.setName("123");
        user2.setBirthday(LocalDate.of(2000, 2, 2));
        user2 = userStorage.addUser(user2);

// User 3 (test3)
        User user3 = new User();
        user3.setLogin("test3");
        user3.setEmail("test3@email.com");
        user3.setName("123");
        user3.setBirthday(LocalDate.of(2000, 2, 2));
        user3 = userStorage.addUser(user3);

// User 4 (test4)
        User user4 = new User();
        user4.setLogin("test4");
        user4.setEmail("test4@email.com");
        user4.setName("123");
        user4.setBirthday(LocalDate.of(2000, 2, 2));
        user4 = userStorage.addUser(user4);

// User 5 (test5)
        User user5 = new User();
        user5.setLogin("test5");
        user5.setEmail("test5@email.com");
        user5.setName("123");
        user5.setBirthday(LocalDate.of(2000, 2, 2));
        user5 = userStorage.addUser(user5);

// Film 2 (film2)
        Film film2 = new Film();
        film2.setName("film1");
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));  // произвольная дата
        film2.setDuration(120);                           // произвольная длительность
        film2 = filmStorage.addFilm(film2);

// Film 3 (poke)
        Film film3 = new Film();
        film3.setName("poke");
        film3.setReleaseDate(LocalDate.of(2001, 2, 2));
        film3.setDuration(95);
        film3 = filmStorage.addFilm(film3);

// Film 4 (cube)
        Film film4 = new Film();
        film4.setName("cube");
        film4.setReleaseDate(LocalDate.of(2002, 3, 3));
        film4.setDuration(110);
        film4 = filmStorage.addFilm(film4);

// Film 5 (dark)
        Film film5 = new Film();
        film5.setName("dark");
        film5.setReleaseDate(LocalDate.of(2003, 4, 4));
        film5.setDuration(105);
        film5 = filmStorage.addFilm(film5);

// Film 6 (starwars)
        Film film6 = new Film();
        film6.setName("starwars");
        film6.setReleaseDate(LocalDate.of(2004, 5, 5));
        film6.setDuration(140);
        film6 = filmStorage.addFilm(film6);

// Film 7 (nigth)
        Film film7 = new Film();
        film7.setName("nigth");
        film7.setReleaseDate(LocalDate.of(2005, 6, 6));
        film7.setDuration(90);
        film7 = filmStorage.addFilm(film7);

// Film 8 (laser)
        Film film8 = new Film();
        film8.setName("laser");
        film8.setReleaseDate(LocalDate.of(2006, 7, 7));
        film8.setDuration(115);
        film8 = filmStorage.addFilm(film8);

// Film 9 (PSgamer)
        Film film9 = new Film();
        film9.setName("PSgamer");
        film9.setReleaseDate(LocalDate.of(2007, 8, 8));
        film9.setDuration(125);
        film9 = filmStorage.addFilm(film9);

// Film 10 (ixbt)
        Film film10 = new Film();
        film10.setName("ixbt");
        film10.setReleaseDate(LocalDate.of(2008, 9, 9));
        film10.setDuration(100);
        film10 = filmStorage.addFilm(film10);

// Film 11 (shiiit)
        Film film11 = new Film();
        film11.setName("shiiit");
        film11.setReleaseDate(LocalDate.of(2009, 10, 10));
        film11.setDuration(80);
        film11 = filmStorage.addFilm(film11);

// Film 12 (warhammer)
        Film film12 = new Film();
        film12.setName("warhammer");
        film12.setReleaseDate(LocalDate.of(2010, 11, 11));
        film12.setDuration(150);
        film12 = filmStorage.addFilm(film12);

// Film 13 (kingdom)
        Film film13 = new Film();
        film13.setName("kingdom");
        film13.setReleaseDate(LocalDate.of(2011, 12, 12));
        film13.setDuration(130);
        film13 = filmStorage.addFilm(film13);

// Film 14 (google)
        Film film14 = new Film();
        film14.setName("google");
        film14.setReleaseDate(LocalDate.of(2012, 1, 13));
        film14.setDuration(95);
        film14 = filmStorage.addFilm(film14);

// Film 15 (123)
        Film film15 = new Film();
        film15.setName("123");
        film15.setReleaseDate(LocalDate.of(2013, 2, 14));
        film15.setDuration(85);
        film15 = filmStorage.addFilm(film15);

// Film 16 (battlefront)
        Film film16 = new Film();
        film16.setName("battlefront");
        film16.setReleaseDate(LocalDate.of(2014, 3, 15));
        film16.setDuration(135);
        film16 = filmStorage.addFilm(film16);

// Film 17 (zombies)
        Film film17 = new Film();
        film17.setName("zombies");
        film17.setReleaseDate(LocalDate.of(2015, 4, 16));
        film17.setDuration(115);
        film17 = filmStorage.addFilm(film17);

// Film 18 (re9)
        Film film18 = new Film();
        film18.setName("re9");
        film18.setReleaseDate(LocalDate.of(2016, 5, 17));
        film18.setDuration(110);
        film18 = filmStorage.addFilm(film18);

// Film 19 (sistem of a down)
        Film film19 = new Film();
        film19.setName("sistem of a down");
        film19.setReleaseDate(LocalDate.of(2017, 6, 18));
        film19.setDuration(145);
        film19 = filmStorage.addFilm(film19);

// Film 20 (gogol)
        Film film20 = new Film();
        film20.setName("gogol");
        film20.setReleaseDate(LocalDate.of(2018, 7, 19));
        film20.setDuration(105);
        film20 = filmStorage.addFilm(film20);

// Film 21 (test)
        Film film21 = new Film();
        film21.setName("test");
        film21.setReleaseDate(LocalDate.of(2019, 8, 20));
        film21.setDuration(90);
        film21 = filmStorage.addFilm(film21);

// User 1 лайкает film2, film3, film4, film5
        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film3.getId(), user1.getId());
        filmStorage.addLike(film5.getId(), user1.getId());
        filmStorage.addLike(film4.getId(), user1.getId());

// User 2 лайкает film2, film21
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film21.getId(), user2.getId());

// User 3 лайкает film18, film2, film3, film4
        filmStorage.addLike(film2.getId(), user3.getId());
        filmStorage.addLike(film3.getId(), user3.getId());
        filmStorage.addLike(film4.getId(), user3.getId());
        filmStorage.addLike(film19.getId(), user3.getId());

// User 4 лайкает film6, film2, film3, film4, film5, film20
        filmStorage.addLike(film6.getId(), user4.getId());
        filmStorage.addLike(film2.getId(), user4.getId());
        filmStorage.addLike(film3.getId(), user4.getId());
        filmStorage.addLike(film4.getId(), user4.getId());
        filmStorage.addLike(film5.getId(), user4.getId());
        filmStorage.addLike(film20.getId(), user4.getId());

// User 5 лайкает film15, film14, film16, film10
        filmStorage.addLike(film15.getId(), user5.getId());
        filmStorage.addLike(film14.getId(), user5.getId());
        filmStorage.addLike(film16.getId(), user5.getId());
        filmStorage.addLike(film10.getId(), user5.getId());

        List<Film> list = filmStorage.getRecommendationsByUserId(user1.getId());

        Assertions.assertThat(list)
                .isNotEmpty()
                .hasSize(4);

        Assertions.assertThat(list)
                .extracting(
                        Film::getId,
                        Film::getName
                )
                .contains(
                        tuple(6L, "starwars"),
                        tuple(20L, "gogol"),
                        tuple(19L, "sistem of a down"),
                        tuple(21L, "test")
                );
    }

    @Test
    public void testGetRecommendedFilmsEmptyList() {
        User user2 = new User();
        user2.setEmail("Test@test2.com");
        user2.setLogin("test2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        User user3 = new User();
        user3.setEmail("Test@test3.com");
        user3.setLogin("test3");
        user3.setBirthday(LocalDate.of(2000, 1, 1));
        User user4 = new User();
        user4.setLogin("test4");
        user4.setEmail("test4@email.com");
        user4.setName("123");
        user4.setBirthday(LocalDate.of(2000, 2, 2));
        User user5 = new User();
        user5.setLogin("test5");
        user5.setEmail("test5@email.com");
        user5.setName("123");
        user5.setBirthday(LocalDate.of(2000, 2, 2));

        Film film2 = new Film();
        film2.setName("TestFilm2");
        film2.setReleaseDate(LocalDate.of(1990, 1, 1));
        film2.setDuration(100);
        Film film3 = new Film();
        film3.setName("TestFilm3");
        film3.setDuration(100);
        film3.setReleaseDate(LocalDate.of(1990, 1, 1));
        Film film4 = new Film();
        film4.setName("TestFilm4");
        film4.setDuration(100);
        film4.setReleaseDate(LocalDate.of(1990, 1, 1));
        Film film5 = new Film();
        film5.setName("TestFilm5");
        film5.setDuration(100);
        film5.setReleaseDate(LocalDate.of(1990, 1, 1));

        user2 = userStorage.addUser(user2);
        user3 = userStorage.addUser(user3);
        user4 = userStorage.addUser(user4);
        user5 = userStorage.addUser(user5);
        film2 = filmStorage.addFilm(film2);
        film3 = filmStorage.addFilm(film3);
        film4 = filmStorage.addFilm(film4);
        film5 = filmStorage.addFilm(film5);

        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film4.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user3.getId());
        filmStorage.addLike(film4.getId(), user3.getId());
        filmStorage.addLike(film5.getId(), user4.getId());
        filmStorage.addLike(film3.getId(), user4.getId());

        List<Film> list = filmStorage.getRecommendationsByUserId(user2.getId());
        Assertions.assertThat(list).isEmpty();

        list = filmStorage.getRecommendationsByUserId(user5.getId());
        Assertions.assertThat(list).isEmpty();
    }

    public void testAddAndFindReview() {
        Review review = new Review();
        review.setContent("Отличный фильм");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);

        Review savedReview = reviewStorage.addReview(review);

        assertThat(savedReview.getReviewId()).isNotNull();

        Optional<Review> reviewOptional = reviewStorage.findById(savedReview.getReviewId());
        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getReviewId()).isEqualTo(savedReview.getReviewId());
                    assertThat(r.getContent()).isEqualTo("Отличный фильм");
                    assertThat(r.getIsPositive()).isEqualTo(true);
                    assertThat(r.getFilmId()).isEqualTo(1L);
                    assertThat(r.getUserId()).isEqualTo(1L);
                });
    }

    @Test
    public void testUpdateReview() {
        Review review = new Review();
        review.setContent("Нормальный фильм");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);

        Review savedReview = reviewStorage.addReview(review);

        savedReview.setContent("Фильм оказался слабым");
        savedReview.setIsPositive(false);

        reviewStorage.updateReview(savedReview);

        Optional<Review> reviewOptional = reviewStorage.findById(savedReview.getReviewId());
        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getContent()).isEqualTo("Фильм оказался слабым");
                    assertThat(r.getIsPositive()).isEqualTo(false);
                });
    }

    @Test
    public void testDeleteReview() {
        Review review = new Review();
        review.setContent("Удаляемый отзыв");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);

        Review savedReview = reviewStorage.addReview(review);
        reviewStorage.deleteReview(savedReview.getReviewId());

        Optional<Review> reviewOptional = reviewStorage.findById(savedReview.getReviewId());
        assertThat(reviewOptional).isNotPresent();
    }

    @Test
    public void testGetReviews() {
        Review review1 = new Review();
        review1.setContent("Первый отзыв");
        review1.setIsPositive(true);
        review1.setFilmId(1L);
        review1.setUserId(1L);
        reviewStorage.addReview(review1);

        User user2 = new User();
        user2.setEmail("review2@test.com");
        user2.setLogin("review2");
        user2.setName("review2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        Review review2 = new Review();
        review2.setContent("Второй отзыв");
        review2.setIsPositive(false);
        review2.setFilmId(1L);
        review2.setUserId(user2.getId());
        reviewStorage.addReview(review2);

        List<Review> reviews = reviewStorage.getReviews();
        assertEquals(2, reviews.size());
    }

    @Test
    public void testGetReviewsByFilmId() {
        Review review1 = new Review();
        review1.setContent("Отзыв к первому фильму");
        review1.setIsPositive(true);
        review1.setFilmId(1L);
        review1.setUserId(1L);
        reviewStorage.addReview(review1);

        Film film2 = new Film();
        film2.setName("Another film");
        film2.setDescription("Another description");
        film2.setDuration(120);
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        film2 = filmStorage.addFilm(film2);

        Review review2 = new Review();
        review2.setContent("Отзыв ко второму фильму");
        review2.setIsPositive(false);
        review2.setFilmId(film2.getId());
        review2.setUserId(1L);
        reviewStorage.addReview(review2);

        List<Review> firstFilmReviews = reviewStorage.getReviewsByFilmId(1L);
        List<Review> secondFilmReviews = reviewStorage.getReviewsByFilmId(film2.getId());

        assertEquals(1, firstFilmReviews.size());
        assertEquals("Отзыв к первому фильму", firstFilmReviews.getFirst().getContent());

        assertEquals(1, secondFilmReviews.size());
        assertEquals("Отзыв ко второму фильму", secondFilmReviews.getFirst().getContent());
    }

    @Test
    public void testAddLikeToReview() {
        Review review = new Review();
        review.setContent("Лайкаемый отзыв");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);
        Review savedReview = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("like@test.com");
        user2.setLogin("likeUser");
        user2.setName("likeUser");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        Review updatedReview = reviewStorage.addLike(savedReview.getReviewId(), user2.getId());

        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getUseful()).isEqualTo(1);
    }

    @Test
    public void testDeleteLikeFromReview() {
        Review review = new Review();
        review.setContent("Отзыв с лайком");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);
        Review savedReview = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("delete_like@test.com");
        user2.setLogin("deleteLikeUser");
        user2.setName("deleteLikeUser");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addLike(savedReview.getReviewId(), user2.getId());
        Review updatedReview = reviewStorage.deleteLike(savedReview.getReviewId(), user2.getId());

        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getUseful()).isEqualTo(0);
    }

    @Test
    public void testAddDislikeToReview() {
        Review review = new Review();
        review.setContent("Дизлайкаемый отзыв");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);
        Review savedReview = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("dislike@test.com");
        user2.setLogin("dislikeUser");
        user2.setName("dislikeUser");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        Review updatedReview = reviewStorage.addDislike(savedReview.getReviewId(), user2.getId());

        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getUseful()).isEqualTo(-1);
    }

    @Test
    public void testDeleteDislikeFromReview() {
        Review review = new Review();
        review.setContent("Отзыв с дизлайком");
        review.setIsPositive(false);
        review.setFilmId(1L);
        review.setUserId(1L);
        Review savedReview = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("delete_dislike@test.com");
        user2.setLogin("deleteDislikeUser");
        user2.setName("deleteDislikeUser");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addDislike(savedReview.getReviewId(), user2.getId());
        Review updatedReview = reviewStorage.deleteDislike(savedReview.getReviewId(), user2.getId());

        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getUseful()).isEqualTo(0);
    }

    @Test
    public void testChangeDislikeToLike() {
        Review review = new Review();
        review.setContent("Смена реакции");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);
        Review savedReview = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("change_reaction@test.com");
        user2.setLogin("changeReactionUser");
        user2.setName("changeReactionUser");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addDislike(savedReview.getReviewId(), user2.getId());
        Review updatedReview = reviewStorage.addLike(savedReview.getReviewId(), user2.getId());

        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getUseful()).isEqualTo(1);
    }

    @Test
    public void testChangeLikeToDislike() {
        Review review = new Review();
        review.setContent("Обратная смена реакции");
        review.setIsPositive(true);
        review.setFilmId(1L);
        review.setUserId(1L);
        Review savedReview = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("change_back@test.com");
        user2.setLogin("changeBackUser");
        user2.setName("changeBackUser");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addLike(savedReview.getReviewId(), user2.getId());
        Review updatedReview = reviewStorage.addDislike(savedReview.getReviewId(), user2.getId());

        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getUseful()).isEqualTo(-1);
    }

    @Test
    public void testGetMostLikedFilmsFilterByYear() {
        User user = userStorage.getUsers().get(0);

        Film film2020 = new Film();
        film2020.setName("Film 2020");
        film2020.setDescription("Description");
        film2020.setDuration(100);
        film2020.setReleaseDate(LocalDate.of(2020, 6, 1));
        film2020.setMpa(new Mpa(1L, "G"));
        film2020 = filmStorage.addFilm(film2020);

        Film film2021 = new Film();
        film2021.setName("Film 2021");
        film2021.setDescription("Description");
        film2021.setDuration(100);
        film2021.setReleaseDate(LocalDate.of(2021, 6, 1));
        film2021.setMpa(new Mpa(1L, "G"));
        film2021 = filmStorage.addFilm(film2021);

        filmStorage.addLike(film2020.getId(), user.getId());

        List<Film> films2020 = filmStorage.getMostLikedFilms(10, null, 2020);
        assertEquals(1, films2020.size());
        assertEquals("Film 2020", films2020.get(0).getName());

        List<Film> films2021 = filmStorage.getMostLikedFilms(10, null, 2021);
        assertEquals(1, films2021.size());
        assertEquals("Film 2021", films2021.get(0).getName());
    }

    @Test
    public void testGetMostLikedFilmsFilterByGenre() {
        User user = userStorage.getUsers().get(0);

        Film drama = new Film();
        drama.setName("Drama Film");
        drama.setDescription("Sad");
        drama.setDuration(120);
        drama.setReleaseDate(LocalDate.of(2020, 7, 1));
        drama.setMpa(new Mpa(1L, "G"));
        drama.setGenres(new HashSet<>(List.of(new Genre(2L, "Драма"))));
        drama = filmStorage.addFilm(drama);

        filmStorage.addLike(drama.getId(), user.getId());

        List<Film> comedyFilms = filmStorage.getMostLikedFilms(10, 1L, null);
        assertEquals(1, comedyFilms.size());
        assertEquals("TestFilm", comedyFilms.get(0).getName());

        List<Film> dramaFilms = filmStorage.getMostLikedFilms(10, 2L, null);
        assertEquals(1, dramaFilms.size());
        assertEquals("Drama Film", dramaFilms.get(0).getName());
    }

    @Test
    public void testGetMostLikedFilmsFilterByGenreAndYear() {
        User user = userStorage.getUsers().get(0);

        Film comedy2020 = new Film();
        comedy2020.setName("Comedy 2020");
        comedy2020.setDescription("Funny 2020");
        comedy2020.setDuration(100);
        comedy2020.setReleaseDate(LocalDate.of(2020, 6, 1));
        comedy2020.setMpa(new Mpa(1L, "G"));
        comedy2020.setGenres(new HashSet<>(List.of(new Genre(1L, "Комедия"))));
        comedy2020 = filmStorage.addFilm(comedy2020);

        Film comedy2021 = new Film();
        comedy2021.setName("Comedy 2021");
        comedy2021.setDescription("Funny 2021");
        comedy2021.setDuration(110);
        comedy2021.setReleaseDate(LocalDate.of(2021, 5, 1));
        comedy2021.setMpa(new Mpa(1L, "G"));
        comedy2021.setGenres(new HashSet<>(List.of(new Genre(1L, "Комедия"))));
        comedy2021 = filmStorage.addFilm(comedy2021);

        filmStorage.addLike(comedy2020.getId(), user.getId());

        List<Film> comedy2020films = filmStorage.getMostLikedFilms(10, 1L, 2020);
        assertEquals(1, comedy2020films.size());
        assertEquals("Comedy 2020", comedy2020films.get(0).getName());

        List<Film> comedy2021films = filmStorage.getMostLikedFilms(10, 1L, 2021);
        assertEquals(1, comedy2021films.size());
        assertEquals("Comedy 2021", comedy2021films.get(0).getName());
    }

    @Test
    public void testGetMostLikedFilmsSortByLikesCount() {
        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        User user1 = userStorage.getUsers().get(0);

        Film film1 = new Film();
        film1.setName("Film with 1 like");
        film1.setDescription("Description");
        film1.setDuration(100);
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setMpa(new Mpa(1L, "G"));
        film1 = filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film with 2 likes");
        film2.setDescription("Description");
        film2.setDuration(100);
        film2.setReleaseDate(LocalDate.of(2020, 2, 1));
        film2.setMpa(new Mpa(1L, "G"));
        film2 = filmStorage.addFilm(film2);

        filmStorage.addLike(film1.getId(), user1.getId());

        filmStorage.addLike(film2.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user2.getId());

        List<Film> popular = filmStorage.getMostLikedFilms(10, null, null);

        assertEquals("Film with 2 likes", popular.get(0).getName());
        assertEquals(2, popular.get(0).getLikes().size());
    }

}
