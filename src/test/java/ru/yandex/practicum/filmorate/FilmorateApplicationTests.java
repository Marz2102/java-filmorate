package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {
	private static final String BASE_URL = "http://localhost:8080/api";
	private final TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	void getEmptyListOfUsers() {
		ResponseEntity<User[]> response = restTemplate.getForEntity(BASE_URL + "/user", User[].class);
		assertEquals(200, response.getStatusCode().value());

		User[] users = response.getBody();
		assertNotNull(users);
        assertEquals(0, users.length);
	}

	@Test
	void getEmptyListOfFilms() {
		ResponseEntity<Film[]> response = restTemplate.getForEntity(BASE_URL + "/film", Film[].class);
		assertEquals(200, response.getStatusCode().value());

		Film[] films = response.getBody();
		assertNotNull(films);
		assertEquals(0, films.length);
	}

	@Test
	void getNotEmptyListOfUsers() {
		User someUser = new User("1@aaa.com", "abc");
		restTemplate.postForEntity(BASE_URL + "/user", someUser, User.class);

		ResponseEntity<User[]> response = restTemplate.getForEntity(BASE_URL + "/user", User[].class);
		assertEquals(200, response.getStatusCode().value());

		User[] users = response.getBody();

		assertNotNull(users);
		assertEquals(1, users.length);
		assertEquals("1@aaa.com", users[0].getEmail());
	}

	@Test
	void getNotEmptyListOfFilms() {
		Film someFilm1 = new Film("Аватар", LocalDate.of(2009, 1, 1), 150);
		Film someFilm2 = new Film("Гладиатор", LocalDate.of(2000, 2, 1), 130);
		restTemplate.postForEntity(BASE_URL + "/film", someFilm1, Film.class);
		restTemplate.postForEntity(BASE_URL + "/film", someFilm2, Film.class);

		ResponseEntity<Film[]> response = restTemplate.getForEntity(BASE_URL + "/film", Film[].class);
		assertEquals(200, response.getStatusCode().value());

		Film[] films = response.getBody();

		assertNotNull(films);
		assertEquals(2, films.length);
		assertEquals(150, films[0].getDuration());
		assertEquals("Гладиатор", films[1].getName());
	}

	@Test
	void postFilmWithoutName() {
		Film film = new Film("", LocalDate.of(2009, 1, 1), 150);

		ResponseEntity<Film> response = restTemplate.postForEntity(BASE_URL + "/film", film, Film.class);
		assertEquals(400, response.getStatusCode().value(), "Должна вернуться ошибка 400");
	}

	@Test
	void postUserWithoutLogin() {
		User user = new User("1@aaa.com", "");

		ResponseEntity<User> response = restTemplate.postForEntity(BASE_URL + "/user", user, User.class);
		assertEquals(400, response.getStatusCode().value(), "Должна вернуться ошибка 400");
	}

	@Test
	void postFilmWithBadReleaseDate() {
		Film film = new Film("Аватар", LocalDate.of(1809, 1, 1), 150);

		ResponseEntity<Film> response = restTemplate.postForEntity(BASE_URL + "/film", film, Film.class);
		assertEquals(500, response.getStatusCode().value(), "Должна вернуться ошибка 500");

	}

	@Test
	void postUserWithBadEmail() {
		User user = new User("1aaa.com", "");

		ResponseEntity<User> response = restTemplate.postForEntity(BASE_URL + "/user", user, User.class);
		assertEquals(400, response.getStatusCode().value(), "Должна вернуться ошибка 400");
	}

	@Test
	void PostEmptyRequestUser() {
		ResponseEntity<User> response = restTemplate.postForEntity(BASE_URL + "/user", null, User.class);
		assertEquals(415, response.getStatusCode().value());
	}

	@Test
	void PostEmptyRequestFilm() {
		ResponseEntity<Film> response = restTemplate.postForEntity(BASE_URL + "/film", null, Film.class);
		assertEquals(415, response.getStatusCode().value());
	}
}
