package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    private GenreDto genreDto;

    @BeforeEach
    void setUp() {
        genreDto = new GenreDto(1L, "Комедия");
    }

    @Test
    void getGenreById_ShouldReturnGenre() throws Exception {
        when(genreService.getGenreById(1L)).thenReturn(genreDto);

        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));
    }

    @Test
    void getGenres_ShouldReturnList() throws Exception {
        List<GenreDto> genres = List.of(
                new GenreDto(1L, "Комедия"),
                new GenreDto(2L, "Драма")
        );
        when(genreService.getGenres()).thenReturn(genres);

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Комедия"))
                .andExpect(jsonPath("$[1].name").value("Драма"));
    }

    @Test
    void getGenreById_NotFound_ShouldReturn404() throws Exception {
        when(genreService.getGenreById(999L)).thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр с id - 999 не найден"));

        mockMvc.perform(get("/genres/999"))
                .andExpect(status().isNotFound());
    }
}
