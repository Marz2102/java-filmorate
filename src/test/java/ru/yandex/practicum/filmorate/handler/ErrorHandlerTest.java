package ru.yandex.practicum.filmorate.handler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmService filmService;

    @Test
    void handleNotFoundException_ShouldReturn404() throws Exception {
        when(filmService.getFilmById(999L))
                .thenThrow(new NotFoundException("Фильм с id - 999 не найден"));

        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.description").value("Фильм с id - 999 не найден"));
    }

    @Test
    void handleValidationException_ShouldReturn400() throws Exception {
        when(filmService.getMostRatedFilms(-1, null, null))
                .thenThrow(new ValidationException("Укажите положительный параметр count"));

        mockMvc.perform(get("/films/popular?count=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.description").value("Укажите положительный параметр count"));
    }

    @Test
    void handleInternalServerException_ShouldReturn500() throws Exception {
        when(filmService.getFilmById(1L))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Ошибка сервера"))
                .andExpect(jsonPath("$.description").value("Unexpected database error"));
    }

    @Test
    void handleUnsupportedMediaTypeException_ShouldReturn415() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<film><name>Test</name></film>"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error").value("Неподдерживаемый тип тела запроса"));
    }
}