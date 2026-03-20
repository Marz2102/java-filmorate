package ru.yandex.practicum.filmorate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.config.TestConfig;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDirectorDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private FilmStorage filmStorage;

    private User testUser;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@test.com");
        testUser.setLogin("test");
        testUser.setName("Test Name");
        testUser.setBirthday(LocalDate.of(2000, 1, 1));
        testUser = userStorage.addUser(testUser);

        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setDuration(120);
        testFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        testFilm = filmStorage.addFilm(testFilm);
    }

    @Test
    void createUser_AndGetUser_ShouldWork() throws Exception {
        UserCreateDto newUser = new UserCreateDto(
                "integration@test.com",
                "integration",
                "Integration User",
                LocalDate.of(1995, 5, 5)
        );

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.login").value("integration"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        FilmDto created = objectMapper.readValue(response, FilmDto.class);
        Long userId = created.getId();

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"));
    }

    @Test
    void createFilm_AndAddLike_ShouldWork() throws Exception {
        FilmCreateDto newFilm = new FilmCreateDto(
                "Integration Film",
                "Integration Description",
                LocalDate.of(2022, 1, 1),
                150,
                null,
                null,
                null
        );

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Film"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        FilmDto created = objectMapper.readValue(response, FilmDto.class);
        Long filmId = created.getId();

        mockMvc.perform(put("/films/{filmId}/like/{userId}", filmId, testUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/{id}", filmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes.length()").value(1));
    }

    @Test
    void createDirector_AndGetFilmsByDirector_ShouldWork() throws Exception {
        DirectorDto director = new DirectorDto(null, "Test Director");

        String directorResponse = mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(director)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        DirectorDto createdDirector = objectMapper.readValue(directorResponse, DirectorDto.class);
        Long directorId = createdDirector.getId();

        List<FilmDirectorDto> directors = List.of(new FilmDirectorDto(directorId));
        FilmCreateDto filmWithDirector = new FilmCreateDto(
                "Film with Director",
                "Description",
                LocalDate.of(2023, 1, 1),
                120,
                null,
                directors,
                null
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmWithDirector)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/films/director/{id}?sortBy=year", directorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void createReview_AndAddLike_ShouldWork() throws Exception {
        ReviewCreateDto review = new ReviewCreateDto(
                "Great movie!",
                testFilm.getId(),
                testUser.getId(),
                true
        );

        String response = mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great movie!"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var created = objectMapper.readValue(response, ru.yandex.practicum.filmorate.dto.review.ReviewDto.class);
        Long reviewId = created.getReviewId();

        mockMvc.perform(put("/reviews/{reviewId}/like/{userId}", reviewId, testUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.useful").value(1));
    }

    @Test
    void addFriend_AndGetCommonFriends_ShouldWork() throws Exception {
        UserCreateDto friend1 = new UserCreateDto(
                "friend1@test.com",
                "friend1",
                "Friend One",
                LocalDate.of(2000, 1, 1)
        );

        UserCreateDto friend2 = new UserCreateDto(
                "friend2@test.com",
                "friend2",
                "Friend Two",
                LocalDate.of(2000, 1, 1)
        );

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(friend1)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(friend2)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var user1 = objectMapper.readValue(response1, ru.yandex.practicum.filmorate.dto.user.UserDto.class);
        var user2 = objectMapper.readValue(response2, ru.yandex.practicum.filmorate.dto.user.UserDto.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", testUser.getId(), user1.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", testUser.getId(), user2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getMostLikedFilms_WithFilters_ShouldWork() throws Exception {
        FilmCreateDto film2020 = new FilmCreateDto(
                "Film 2020",
                "Description",
                LocalDate.of(2020, 6, 1),
                100,
                List.of(),
                null,
                null
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film2020)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/films/popular?year=2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.name == 'Film 2020')]").exists());
    }

    @Test
    void fullUserFlow_CreateUpdateDelete_ShouldWork() throws Exception {
        UserCreateDto newUser = new UserCreateDto(
                "flow@test.com",
                "flow",
                "Flow User",
                LocalDate.of(1990, 1, 1)
        );

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var created = objectMapper.readValue(response, ru.yandex.practicum.filmorate.dto.user.UserDto.class);
        Long userId = created.getId();

        ru.yandex.practicum.filmorate.dto.user.UserUpdateDto updateDto = new ru.yandex.practicum.filmorate.dto.user.UserUpdateDto(
                userId,
                "updated@test.com",
                "updated",
                "Updated User",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullFilmFlow_CreateUpdateDelete_ShouldWork() throws Exception {
        FilmCreateDto newFilm = new FilmCreateDto(
                "Flow Film",
                "Flow Description",
                LocalDate.of(2024, 1, 1),
                200,
                null,
                null,
                null
        );

        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilm)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        FilmDto created = objectMapper.readValue(response, FilmDto.class);
        Long filmId = created.getId();

        ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto updateDto = new ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto(
                filmId,
                "Updated Flow Film",
                "Updated Description",
                LocalDate.of(2024, 1, 1),
                220,
                null
        );

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Flow Film"))
                .andExpect(jsonPath("$.duration").value(220));

        mockMvc.perform(delete("/films/{filmId}", filmId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/films/{id}", filmId))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationErrors_ShouldReturnBadRequest() throws Exception {
        UserCreateDto invalidUser = new UserCreateDto(
                "invalid-email",
                "",
                null,
                LocalDate.of(2030, 1, 1)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        FilmCreateDto invalidFilm = new FilmCreateDto(
                "",
                "a".repeat(201),
                LocalDate.of(1890, 1, 1),
                -10,
                null,
                null,
                null
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }
}