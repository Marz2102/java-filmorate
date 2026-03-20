package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReviewDbStorageTest {

    @Autowired
    @Qualifier("ReviewDao")
    private ReviewStorage reviewStorage;

    @Autowired
    @Qualifier("UserDao")
    private UserStorage userStorage;

    @Autowired
    @Qualifier("FilmDao")
    private FilmStorage filmStorage;

    private User testUser;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setLogin("test");
        testUser.setName("TestName");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));
        testUser = userStorage.addUser(testUser);

        testFilm = new Film();
        testFilm.setName("TestFilm");
        testFilm.setDescription("TestDescription");
        testFilm.setDuration(100);
        testFilm.setReleaseDate(LocalDate.of(1990, 12, 12));
        testFilm.setMpa(new Mpa(1L, "G"));
        testFilm = filmStorage.addFilm(testFilm);
    }

    @Test
    void addReview_ShouldGenerateId() {
        Review review = new Review();
        review.setContent("Great movie!");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());

        Review saved = reviewStorage.addReview(review);

        assertThat(saved.getReviewId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo("Great movie!");
        assertThat(saved.getUseful()).isEqualTo(0);
    }

    @Test
    void findById_ShouldReturnReview() {
        Review review = new Review();
        review.setContent("Great movie!");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        Optional<Review> reviewOptional = reviewStorage.findById(saved.getReviewId());

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getContent()).isEqualTo("Great movie!");
                    assertThat(r.getIsPositive()).isTrue();
                });
    }

    @Test
    void updateReview_ShouldUpdateFields() {
        Review review = new Review();
        review.setContent("Original content");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        saved.setContent("Updated content");
        saved.setIsPositive(false);
        reviewStorage.updateReview(saved);

        Optional<Review> fromDb = reviewStorage.findById(saved.getReviewId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getContent()).isEqualTo("Updated content");
                    assertThat(r.getIsPositive()).isFalse();
                });
    }

    @Test
    void deleteReview_ShouldRemoveReview() {
        Review review = new Review();
        review.setContent("To delete");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        reviewStorage.deleteReview(saved.getReviewId());

        Optional<Review> fromDb = reviewStorage.findById(saved.getReviewId());
        assertThat(fromDb).isNotPresent();
    }

    @Test
    void getReviews_ShouldReturnAllReviews() {
        Film secondFilm = new Film();
        secondFilm.setName("Second Film");
        secondFilm.setDescription("Second Description");
        secondFilm.setDuration(100);
        secondFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        secondFilm.setMpa(new Mpa(1L, "G"));
        secondFilm = filmStorage.addFilm(secondFilm);

        Review review1 = new Review();
        review1.setContent("First review");
        review1.setIsPositive(true);
        review1.setFilmId(testFilm.getId());
        review1.setUserId(testUser.getId());
        reviewStorage.addReview(review1);

        Review review2 = new Review();
        review2.setContent("Second review");
        review2.setIsPositive(false);
        review2.setFilmId(secondFilm.getId());
        review2.setUserId(testUser.getId());
        reviewStorage.addReview(review2);

        List<Review> reviews = reviewStorage.getReviews();
        assertEquals(2, reviews.size());
    }

    @Test
    void getReviewsByFilmId_ShouldReturnReviewsForFilm() {
        Review review1 = new Review();
        review1.setContent("Review for film 1");
        review1.setIsPositive(true);
        review1.setFilmId(testFilm.getId());
        review1.setUserId(testUser.getId());
        reviewStorage.addReview(review1);

        Film film2 = new Film();
        film2.setName("Another film");
        film2.setDescription("Description");
        film2.setDuration(120);
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setMpa(new Mpa(1L, "G"));
        film2 = filmStorage.addFilm(film2);

        Review review2 = new Review();
        review2.setContent("Review for film 2");
        review2.setIsPositive(false);
        review2.setFilmId(film2.getId());
        review2.setUserId(testUser.getId());
        reviewStorage.addReview(review2);

        List<Review> film1Reviews = reviewStorage.getReviewsByFilmId(testFilm.getId());
        List<Review> film2Reviews = reviewStorage.getReviewsByFilmId(film2.getId());

        assertEquals(1, film1Reviews.size());
        assertEquals("Review for film 1", film1Reviews.getFirst().getContent());
        assertEquals(1, film2Reviews.size());
        assertEquals("Review for film 2", film2Reviews.getFirst().getContent());
    }

    @Test
    void addLike_ShouldIncreaseUseful() {
        Review review = new Review();
        review.setContent("Liked review");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        Review updated = reviewStorage.addLike(saved.getReviewId(), user2.getId());

        assertThat(updated.getUseful()).isEqualTo(1);
    }

    @Test
    void deleteLike_ShouldDecreaseUseful() {
        Review review = new Review();
        review.setContent("Review with like");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addLike(saved.getReviewId(), user2.getId());
        Review updated = reviewStorage.deleteLike(saved.getReviewId(), user2.getId());

        assertThat(updated.getUseful()).isEqualTo(0);
    }

    @Test
    void addDislike_ShouldDecreaseUseful() {
        Review review = new Review();
        review.setContent("Disliked review");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        Review updated = reviewStorage.addDislike(saved.getReviewId(), user2.getId());

        assertThat(updated.getUseful()).isEqualTo(-1);
    }

    @Test
    void deleteDislike_ShouldIncreaseUseful() {
        Review review = new Review();
        review.setContent("Review with dislike");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addDislike(saved.getReviewId(), user2.getId());
        Review updated = reviewStorage.deleteDislike(saved.getReviewId(), user2.getId());

        assertThat(updated.getUseful()).isEqualTo(0);
    }

    @Test
    void changeDislikeToLike_ShouldUpdateUsefulCorrectly() {
        Review review = new Review();
        review.setContent("Change reaction");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addDislike(saved.getReviewId(), user2.getId());
        Review updated = reviewStorage.addLike(saved.getReviewId(), user2.getId());

        assertThat(updated.getUseful()).isEqualTo(1);
    }

    @Test
    void changeLikeToDislike_ShouldUpdateUsefulCorrectly() {
        Review review = new Review();
        review.setContent("Change reaction back");
        review.setIsPositive(true);
        review.setFilmId(testFilm.getId());
        review.setUserId(testUser.getId());
        Review saved = reviewStorage.addReview(review);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        user2 = userStorage.addUser(user2);

        reviewStorage.addLike(saved.getReviewId(), user2.getId());
        Review updated = reviewStorage.addDislike(saved.getReviewId(), user2.getId());

        assertThat(updated.getUseful()).isEqualTo(-1);
    }
}