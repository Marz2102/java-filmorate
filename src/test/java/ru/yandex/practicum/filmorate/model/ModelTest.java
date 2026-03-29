package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;

class ModelTest {

    @Test
    void userBuilder_ShouldCreateCorrectUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setLogin("test");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setFriends(new HashMap<>());

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getLogin()).isEqualTo("test");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getBirthday()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(user.getFriends()).isEmpty();
    }

    @Test
    void userEqualsAndHashCode_ShouldWorkCorrectly() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("test@test.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setEmail("different@test.com");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void filmBuilder_ShouldCreateCorrectFilm() {
        Film film = new Film();
        film.setId(1L);
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setDirectors(new HashSet<>());
        film.setGenres(new HashSet<>());

        assertThat(film.getId()).isEqualTo(1L);
        assertThat(film.getName()).isEqualTo("Inception");
        assertThat(film.getDescription()).isEqualTo("A mind-bending thriller");
        assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(2010, 7, 16));
        assertThat(film.getDuration()).isEqualTo(148);
        assertThat(film.getDirectors()).isEmpty();
        assertThat(film.getGenres()).isEmpty();
    }

    @Test
    void filmEqualsAndHashCode_ShouldWorkCorrectly() {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Inception");

        Film film2 = new Film();
        film2.setId(1L);
        film2.setName("Different Name");

        assertThat(film1).isEqualTo(film2);
        assertThat(film1.hashCode()).isEqualTo(film2.hashCode());
    }

    @Test
    void reviewBuilder_ShouldCreateCorrectReview() {
        Review review = new Review();
        review.setReviewId(1L);
        review.setContent("Great movie!");
        review.setIsPositive(true);
        review.setUserId(1L);
        review.setFilmId(1L);
        review.setUseful(10);

        assertThat(review.getReviewId()).isEqualTo(1L);
        assertThat(review.getContent()).isEqualTo("Great movie!");
        assertThat(review.getIsPositive()).isTrue();
        assertThat(review.getUserId()).isEqualTo(1L);
        assertThat(review.getFilmId()).isEqualTo(1L);
        assertThat(review.getUseful()).isEqualTo(10);
    }

    @Test
    void directorBuilder_ShouldCreateCorrectDirector() {
        Director director = new Director();
        director.setId(1L);
        director.setName("Christopher Nolan");

        assertThat(director.getId()).isEqualTo(1L);
        assertThat(director.getName()).isEqualTo("Christopher Nolan");
    }

    @Test
    void genreBuilder_ShouldCreateCorrectGenre() {
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Комедия");

        assertThat(genre.getId()).isEqualTo(1L);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    void mpaBuilder_ShouldCreateCorrectMpa() {
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("G");

        assertThat(mpa.getId()).isEqualTo(1L);
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void eventBuilder_ShouldCreateCorrectEvent() {
        Event event = new Event();
        event.setEventId(1L);
        event.setUserId(1L);
        event.setEntityId(1L);
        event.setEventType(EventType.LIKE);
        event.setOperation(Operation.ADD);
        event.setCreationDateTime(new java.sql.Timestamp(System.currentTimeMillis()));

        assertThat(event.getEventId()).isEqualTo(1L);
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getEntityId()).isEqualTo(1L);
        assertThat(event.getEventType()).isEqualTo(EventType.LIKE);
        assertThat(event.getOperation()).isEqualTo(Operation.ADD);
        assertThat(event.getCreationDateTime()).isNotNull();
    }

    @Test
    void eventTypeFrom_ShouldReturnCorrectEnum() {
        assertThat(EventType.from("LIKE")).isEqualTo(EventType.LIKE);
        assertThat(EventType.from("REVIEW")).isEqualTo(EventType.REVIEW);
        assertThat(EventType.from("FRIEND")).isEqualTo(EventType.FRIEND);
    }

    @Test
    void operationFrom_ShouldReturnCorrectEnum() {
        assertThat(Operation.from("ADD")).isEqualTo(Operation.ADD);
        assertThat(Operation.from("UPDATE")).isEqualTo(Operation.UPDATE);
        assertThat(Operation.from("REMOVE")).isEqualTo(Operation.REMOVE);
    }

    @Test
    void friendshipStatus_ShouldHaveCorrectValues() {
        assertThat(FriendshipStatus.CONFIRMED).isNotNull();
        assertThat(FriendshipStatus.NOT_CONFIRMED).isNotNull();
    }
}