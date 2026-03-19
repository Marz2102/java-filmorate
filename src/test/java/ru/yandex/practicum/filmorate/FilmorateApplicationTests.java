package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
