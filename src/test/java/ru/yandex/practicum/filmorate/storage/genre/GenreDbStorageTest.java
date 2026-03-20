package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreDbStorageTest {

    @Autowired
    @Qualifier("GenreDao")
    private GenreStorage genreStorage;

    @Test
    void findById_ShouldReturnGenre() {
        Optional<Genre> genreOptional = genreStorage.findById(5L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("name", "Документальный")
                );
    }

    @Test
    void getGenres_ShouldReturnAllGenres() {
        List<Genre> genres = genreStorage.getGenres();

        assertEquals(6, genres.size());
        assertEquals("Комедия", genres.get(0).getName());
        assertEquals("Драма", genres.get(1).getName());
        assertEquals("Мультфильм", genres.get(2).getName());
        assertEquals("Триллер", genres.get(3).getName());
        assertEquals("Документальный", genres.get(4).getName());
        assertEquals("Боевик", genres.get(5).getName());
    }
}