package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDbStorageTest {

    @Autowired
    @Qualifier("FilmDao")
    private FilmStorage filmStorage;

    @Autowired
    @Qualifier("UserDao")
    private UserStorage userStorage;

    @Autowired
    @Qualifier("GenreDao")
    private GenreStorage genreStorage;

    @Autowired
    @Qualifier("MpaDao")
    private MpaStorage mpaStorage;

    @Autowired
    @Qualifier("DirectorDao")
    private DirectorStorage directorStorage;

    private Film testFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setLogin("test");
        testUser.setName("TestName");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.addUser(testUser);

        testFilm = new Film();
        testFilm.setName("TestFilm");
        testFilm.setDescription("TestDescription");
        testFilm.setDuration(100);
        testFilm.setReleaseDate(LocalDate.of(1990, 12, 12));
        testFilm.setGenres(new HashSet<>(Set.of(
                new Genre(1L, "Комедия"),
                new Genre(3L, "Мультфильм")
        )));
        testFilm.setMpa(new Mpa(1L, "G"));
        filmStorage.addFilm(testFilm);
    }

    @Test
    void findById_ShouldReturnFilm() {
        Optional<Film> filmOptional = filmStorage.findById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    void getFilms_ShouldReturnAllFilms() {
        List<Film> films = filmStorage.getFilms();
        assertEquals(1, films.size());

        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("Description");
        newFilm.setDuration(120);
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmStorage.addFilm(newFilm);

        films = filmStorage.getFilms();
        assertEquals(2, films.size());
    }

    @Test
    void addFilm_ShouldGenerateId() {
        Film newFilm = new Film();
        newFilm.setName("Brand New");
        newFilm.setDescription("Brand new description");
        newFilm.setDuration(150);
        newFilm.setReleaseDate(LocalDate.of(2020, 5, 5));

        Film saved = filmStorage.addFilm(newFilm);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Brand New");
    }

    @Test
    void updateFilm_ShouldUpdateFields() {
        testFilm.setName("Updated Film");
        testFilm.setDescription("Updated Description");

        Film updated = filmStorage.updateFilm(testFilm);

        assertEquals("Updated Film", updated.getName());
        assertEquals("Updated Description", updated.getDescription());

        Optional<Film> fromDb = filmStorage.findById(testFilm.getId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("Updated Film");
                    assertThat(film.getDescription()).isEqualTo("Updated Description");
                });
    }

    @Test
    void addLike_ShouldAddToLikes() {
        filmStorage.addLike(1L, 1L);

        Optional<Film> film = filmStorage.findById(1L);
        assertThat(film).isPresent();
        assertEquals(1, film.get().getLikes().size());
    }

    @Test
    void deleteLike_ShouldRemoveFromLikes() {
        filmStorage.addLike(1L, 1L);
        filmStorage.deleteLike(1L, 1L);

        Optional<Film> film = filmStorage.findById(1L);
        assertThat(film).isPresent();
        assertEquals(0, film.get().getLikes().size());
    }

    @Test
    void getMostLikedFilms_ShouldReturnSortedByLikes() {
        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.addUser(user2);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description");
        film2.setDuration(100);
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        filmStorage.addFilm(film2);

        filmStorage.addLike(1L, 1L);
        filmStorage.addLike(2L, 1L);
        filmStorage.addLike(2L, user2.getId());

        List<Film> popular = filmStorage.getMostLikedFilms(10);

        assertEquals(2, popular.size());
        assertEquals(2L, popular.get(0).getId());
        assertEquals(1L, popular.get(1).getId());
    }

    @Test
    void getMostLikedFilms_FilterByYear_ShouldReturnFilmsFromYear() {
        Film film2020 = new Film();
        film2020.setName("Film 2020");
        film2020.setDescription("Description");
        film2020.setDuration(100);
        film2020.setReleaseDate(LocalDate.of(2020, 6, 1));
        film2020.setMpa(new Mpa(1L, "G"));
        film2020 = filmStorage.addFilm(film2020);

        filmStorage.addLike(film2020.getId(), testUser.getId());

        List<Film> films2020 = filmStorage.getMostLikedFilms(10, null, 2020);
        assertEquals(1, films2020.size());
        assertEquals("Film 2020", films2020.get(0).getName());
    }

    @Test
    void getMostLikedFilms_FilterByGenre_ShouldReturnFilmsByGenre() {
        Film drama = new Film();
        drama.setName("Drama Film");
        drama.setDescription("Sad");
        drama.setDuration(120);
        drama.setReleaseDate(LocalDate.of(2020, 7, 1));
        drama.setMpa(new Mpa(1L, "G"));
        drama.setGenres(new HashSet<>(Set.of(new Genre(2L, "Драма"))));
        drama = filmStorage.addFilm(drama);

        filmStorage.addLike(drama.getId(), testUser.getId());

        List<Film> comedyFilms = filmStorage.getMostLikedFilms(10, 1L, null);
        assertEquals(1, comedyFilms.size());
        assertEquals("TestFilm", comedyFilms.get(0).getName());

        List<Film> dramaFilms = filmStorage.getMostLikedFilms(10, 2L, null);
        assertEquals(1, dramaFilms.size());
        assertEquals("Drama Film", dramaFilms.get(0).getName());
    }

    @Test
    void deleteFilm_ShouldRemoveFilm() {
        Film filmToDelete = new Film();
        filmToDelete.setName("To Delete");
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
    void deleteNonExistentFilm_ShouldThrowException() {
        assertThrows(ResponseStatusException.class, () -> {
            filmStorage.deleteFilm(999L);
        });
    }

    @Test
    void getCommonFilms_ShouldReturnFilmsLikedByBothUsers() {
        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.addUser(user2);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description");
        film2.setDuration(100);
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        filmStorage.addFilm(film2);

        filmStorage.addLike(1L, testUser.getId());
        filmStorage.addLike(1L, user2.getId());

        List<Film> commonFilms = filmStorage.getCommonFilms(testUser.getId(), user2.getId());
        assertEquals(1, commonFilms.size());
        assertEquals(1L, commonFilms.get(0).getId());
    }

    @Test
    void searchFilms_ByTitle_ShouldReturnMatchingFilms() {
        Film film1 = new Film();
        film1.setName("Крадущийся тигр");
        film1.setDescription("desc");
        film1.setDuration(100);
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setMpa(new Mpa(1L, "G"));
        filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Другой фильм");
        film2.setDescription("desc");
        film2.setDuration(100);
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        filmStorage.addFilm(film2);

        List<Film> result = filmStorage.searchFilms("крад", "title");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Крадущийся");
    }

    @Test
    void searchFilms_ByDirector_ShouldReturnMatchingFilms() {
        Director director = new Director();
        director.setName("Кристофер Нолан");
        director = directorStorage.addDirector(director);

        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Description");
        film.setDuration(148);
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setMpa(new Mpa(1L, "G"));
        film.setDirectors(Set.of(director));
        filmStorage.addFilm(film);

        List<Film> result = filmStorage.searchFilms("нолан", "director");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Inception");
    }

    @Test
    void searchFilms_ByBoth_ShouldReturnMatchingFilms() {
        Director director = new Director();
        director.setName("Кристофер Нолан");
        director = directorStorage.addDirector(director);

        Film film1 = new Film();
        film1.setName("Inception");
        film1.setDescription("Description");
        film1.setDuration(148);
        film1.setReleaseDate(LocalDate.of(2010, 7, 16));
        film1.setMpa(new Mpa(1L, "G"));
        film1.setDirectors(Set.of(director));
        filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Крадущийся тигр");
        film2.setDescription("Description");
        film2.setDuration(100);
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        filmStorage.addFilm(film2);

        List<Film> result = filmStorage.searchFilms("ин", "director,title");

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(Film::getId).distinct().count()).isEqualTo(2);
    }
}