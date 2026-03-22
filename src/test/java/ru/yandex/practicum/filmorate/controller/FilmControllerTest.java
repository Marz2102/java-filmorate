package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private FilmDto filmDto;
    private FilmCreateDto filmCreateDto;

    @BeforeEach
    void setUp() {
        filmDto = new FilmDto();
        filmDto.setId(1L);
        filmDto.setName("Inception");
        filmDto.setDescription("A mind-bending thriller");
        filmDto.setReleaseDate(LocalDate.of(2010, 7, 16));
        filmDto.setDuration(148);

        filmCreateDto = new FilmCreateDto(
                "Inception",
                "A mind-bending thriller",
                LocalDate.of(2010, 7, 16),
                148,
                null,
                null,
                null
        );
    }

    @Test
    void getFilmById_ShouldReturnFilm() throws Exception {
        when(filmService.getFilmById(1L)).thenReturn(filmDto);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Inception"))
                .andExpect(jsonPath("$.duration").value(148));
    }

    @Test
    void getFilms_ShouldReturnList() throws Exception {
        when(filmService.getFilms()).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addFilm_ShouldReturnCreated() throws Exception {
        when(filmService.addFilm(any(FilmCreateDto.class))).thenReturn(filmDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Inception"));
    }

    @Test
    void addFilm_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        FilmCreateDto invalidDto = new FilmCreateDto(
                "",
                "Description",
                LocalDate.of(2010, 7, 16),
                148,
                null,
                null,
                null
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).addFilm(any());
    }

    @Test
    void addFilm_WithDescriptionTooLong_ShouldReturnBadRequest() throws Exception {
        String longDescription = "a".repeat(201);
        FilmCreateDto invalidDto = new FilmCreateDto(
                "Inception",
                longDescription,
                LocalDate.of(2010, 7, 16),
                148,
                null,
                null,
                null
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).addFilm(any());
    }

    @Test
    void addFilm_WithNegativeDuration_ShouldReturnBadRequest() throws Exception {
        FilmCreateDto invalidDto = new FilmCreateDto(
                "Inception",
                "Description",
                LocalDate.of(2010, 7, 16),
                -10,
                null,
                null,
                null
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).addFilm(any());
    }

    @Test
    void addFilm_WithReleaseDateBeforeCinemaBirthday_ShouldReturnBadRequest() throws Exception {
        FilmCreateDto invalidDto = new FilmCreateDto(
                "Inception",
                "Description",
                LocalDate.of(1890, 1, 1),
                148,
                null,
                null,
                null
        );

        when(filmService.addFilm(any(FilmCreateDto.class)))
                .thenThrow(new ValidationException("Дата релиза не может быть раньше 1895-12-28"));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(filmService).addFilm(any(FilmCreateDto.class));
    }

    @Test
    void updateFilm_ShouldReturnOk() throws Exception {
        FilmUpdateDto updateDto = new FilmUpdateDto(
                1L,
                "Updated Film",
                "Updated description",
                LocalDate.of(2010, 7, 16),
                150,
                null
        );

        FilmDto updatedDto = new FilmDto();
        updatedDto.setId(1L);
        updatedDto.setName("Updated Film");
        updatedDto.setDescription("Updated description");
        updatedDto.setReleaseDate(LocalDate.of(2010, 7, 16));
        updatedDto.setDuration(150);

        when(filmService.updateFilm(any(FilmUpdateDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Film"))
                .andExpect(jsonPath("$.duration").value(150));
    }

    @Test
    void updateFilm_WithoutId_ShouldReturnBadRequest() throws Exception {
        FilmUpdateDto invalidDto = new FilmUpdateDto(
                null,
                "Updated Film",
                "Description",
                LocalDate.of(2010, 7, 16),
                150,
                null
        );

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).updateFilm(any());
    }

    @Test
    void addLike_ShouldReturnOk() throws Exception {
        when(filmService.addLike(1L, 1L)).thenReturn(filmDto);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk());

        verify(filmService).addLike(1L, 1L);
    }

    @Test
    void deleteLike_ShouldReturnOk() throws Exception {
        when(filmService.deleteLike(1L, 1L)).thenReturn(filmDto);

        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk());

        verify(filmService).deleteLike(1L, 1L);
    }

    @Test
    void getMostLikedFilms_WithDefaultParams_ShouldReturnList() throws Exception {
        when(filmService.getMostLikedFilms(10, null, null)).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(filmService).getMostLikedFilms(10, null, null);
    }

    @Test
    void getMostLikedFilms_WithCount_ShouldReturnList() throws Exception {
        when(filmService.getMostLikedFilms(5, null, null)).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk());

        verify(filmService).getMostLikedFilms(5, null, null);
    }

    @Test
    void getMostLikedFilms_WithGenreId_ShouldReturnList() throws Exception {
        when(filmService.getMostLikedFilms(10, 1L, null)).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/popular?genreId=1"))
                .andExpect(status().isOk());

        verify(filmService).getMostLikedFilms(10, 1L, null);
    }

    @Test
    void getMostLikedFilms_WithYear_ShouldReturnList() throws Exception {
        when(filmService.getMostLikedFilms(10, null, 2020)).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/popular?year=2020"))
                .andExpect(status().isOk());

        verify(filmService).getMostLikedFilms(10, null, 2020);
    }

    @Test
    void getMostLikedFilms_WithAllFilters_ShouldReturnList() throws Exception {
        when(filmService.getMostLikedFilms(5, 1L, 2020)).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/popular?count=5&genreId=1&year=2020"))
                .andExpect(status().isOk());

        verify(filmService).getMostLikedFilms(5, 1L, 2020);
    }

    @Test
    void getAllFilmsByDirectorId_SortByLikes_ShouldReturnList() throws Exception {
        when(filmService.getAllFilmsByDirectorId(1L, "likes")).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/director/1?sortBy=likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(filmService).getAllFilmsByDirectorId(1L, "likes");
    }

    @Test
    void getAllFilmsByDirectorId_SortByYear_ShouldReturnList() throws Exception {
        when(filmService.getAllFilmsByDirectorId(1L, "year")).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/director/1?sortBy=year"))
                .andExpect(status().isOk());

        verify(filmService).getAllFilmsByDirectorId(1L, "year");
    }

    @Test
    void getAllFilmsByDirectorId_WithInvalidSortBy_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/films/director/1?sortBy=invalid"))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).getAllFilmsByDirectorId(anyLong(), anyString());
    }

    @Test
    void getCommonFilms_ShouldReturnList() throws Exception {
        when(filmService.getCommonFilms(1L, 2L)).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/common?userId=1&friendId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(filmService).getCommonFilms(1L, 2L);
    }

    @Test
    void deleteFilm_ShouldReturnNoContent() throws Exception {
        doNothing().when(filmService).deleteFilm(1L);

        mockMvc.perform(delete("/films/1"))
                .andExpect(status().isNoContent());

        verify(filmService).deleteFilm(1L);
    }

    @Test
    void searchFilms_ByTitle_ShouldReturnList() throws Exception {
        when(filmService.searchFilms("крад", "title")).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/search?query=крад&by=title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(filmService).searchFilms("крад", "title");
    }

    @Test
    void searchFilms_ByDirector_ShouldReturnList() throws Exception {
        when(filmService.searchFilms("нолан", "director")).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/search?query=нолан&by=director"))
                .andExpect(status().isOk());

        verify(filmService).searchFilms("нолан", "director");
    }

    @Test
    void searchFilms_ByBoth_ShouldReturnList() throws Exception {
        when(filmService.searchFilms("крад", "director,title")).thenReturn(List.of(filmDto));

        mockMvc.perform(get("/films/search?query=крад&by=director,title"))
                .andExpect(status().isOk());

        verify(filmService).searchFilms("крад", "director,title");
    }

    @Test
    void searchFilms_WithInvalidBy_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/films/search?query=крад&by=invalid"))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).searchFilms(anyString(), anyString());
    }

    @Test
    void searchFilms_WithEmptyBy_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/films/search?query=крад&by="))
                .andExpect(status().isBadRequest());

        verify(filmService, never()).searchFilms(anyString(), anyString());
    }

}