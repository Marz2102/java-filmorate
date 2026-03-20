package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DirectorDbStorageTest {

    @Autowired
    @Qualifier("DirectorDao")
    private DirectorStorage directorStorage;

    @Autowired
    @Qualifier("FilmDao")
    private FilmStorage filmStorage;

    @Autowired
    @Qualifier("UserDao")
    private UserStorage userStorage;

    private Director testDirector;

    @BeforeEach
    void setUp() {
        testDirector = new Director();
        testDirector.setName("Christopher Nolan");
        testDirector = directorStorage.addDirector(testDirector);
    }

    @Test
    void findById_ShouldReturnDirector() {
        Optional<Director> directorOptional = directorStorage.findById(testDirector.getId());

        assertThat(directorOptional)
                .isPresent()
                .hasValueSatisfying(director ->
                        assertThat(director).hasFieldOrPropertyWithValue("name", "Christopher Nolan")
                );
    }

    @Test
    void getDirectors_ShouldReturnAllDirectors() {
        List<Director> directors = directorStorage.getDirectors();
        assertEquals(1, directors.size());

        Director newDirector = new Director();
        newDirector.setName("Quentin Tarantino");
        directorStorage.addDirector(newDirector);

        directors = directorStorage.getDirectors();
        assertEquals(2, directors.size());
    }

    @Test
    void addDirector_ShouldGenerateId() {
        Director newDirector = new Director();
        newDirector.setName("Martin Scorsese");

        Director saved = directorStorage.addDirector(newDirector);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Martin Scorsese");
    }

    @Test
    void updateDirector_ShouldUpdateName() {
        testDirector.setName("Updated Name");
        Director updated = directorStorage.updateDirector(testDirector);

        assertEquals("Updated Name", updated.getName());

        Optional<Director> fromDb = directorStorage.findById(testDirector.getId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(director ->
                        assertThat(director.getName()).isEqualTo("Updated Name")
                );
    }

    @Test
    void deleteDirector_ShouldRemoveDirector() {
        Director toDelete = new Director();
        toDelete.setName("To Delete");
        toDelete = directorStorage.addDirector(toDelete);

        directorStorage.deleteDirector(toDelete.getId());

        Optional<Director> fromDb = directorStorage.findById(toDelete.getId());
        assertThat(fromDb).isNotPresent();
    }

    @Test
    void getFilmsByDirectorSortedByLikes_ShouldReturnFilmsSortedByLikes() {
        Director director = directorStorage.addDirector(new Director(null, "Likes Director"));

        Film film1 = new Film();
        film1.setName("Film1");
        film1.setDescription("desc1");
        film1.setDuration(100);
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setMpa(new Mpa(1L, "G"));
        film1.setDirectors(new HashSet<>(Set.of(director)));
        film1 = filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film2");
        film2.setDescription("desc2");
        film2.setDuration(110);
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        film2.setDirectors(new HashSet<>(Set.of(director)));
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

        List<Film> films = filmStorage.getFilmsByDirectorId(director.getId(), "likes");

        assertEquals(2, films.size());
        assertEquals(film2.getId(), films.get(0).getId());
        assertEquals(film1.getId(), films.get(1).getId());
    }

    @Test
    void getFilmsByDirectorSortedByYear_ShouldReturnFilmsSortedByYear() {
        Director director = directorStorage.addDirector(new Director(null, "Year Director"));

        Film oldFilm = new Film();
        oldFilm.setName("Old");
        oldFilm.setDescription("old");
        oldFilm.setDuration(90);
        oldFilm.setReleaseDate(LocalDate.of(1990, 1, 1));
        oldFilm.setMpa(new Mpa(1L, "G"));
        oldFilm.setDirectors(new HashSet<>(Set.of(director)));
        oldFilm = filmStorage.addFilm(oldFilm);

        Film newFilm = new Film();
        newFilm.setName("New");
        newFilm.setDescription("new");
        newFilm.setDuration(100);
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setMpa(new Mpa(1L, "G"));
        newFilm.setDirectors(new HashSet<>(Set.of(director)));
        newFilm = filmStorage.addFilm(newFilm);

        List<Film> films = filmStorage.getFilmsByDirectorId(director.getId(), "year");

        assertEquals(2, films.size());
        assertEquals(oldFilm.getId(), films.get(0).getId());
        assertEquals(newFilm.getId(), films.get(1).getId());
    }
}
