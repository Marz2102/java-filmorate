package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DirectorController.class)
class DirectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DirectorService directorService;

    private DirectorDto directorDto;

    @BeforeEach
    void setUp() {
        directorDto = new DirectorDto(1L, "Christopher Nolan");
    }

    @Test
    void getDirectorById_ShouldReturnDirector() throws Exception {
        when(directorService.getDirectorById(1L)).thenReturn(directorDto);

        mockMvc.perform(get("/directors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Christopher Nolan"));
    }

    @Test
    void getAllDirectors_ShouldReturnList() throws Exception {
        when(directorService.getDirectors()).thenReturn(List.of(directorDto));

        mockMvc.perform(get("/directors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addDirector_ShouldReturnCreated() throws Exception {
        DirectorDto inputDto = new DirectorDto(null, "Quentin Tarantino");
        DirectorDto savedDto = new DirectorDto(2L, "Quentin Tarantino");

        when(directorService.addDirector(any(DirectorDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Quentin Tarantino"));
    }

    @Test
    void addDirector_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        DirectorDto invalidDto = new DirectorDto(null, "");

        mockMvc.perform(post("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(directorService, never()).addDirector(any());
    }

    @Test
    void updateDirector_ShouldReturnOk() throws Exception {
        DirectorUpdateDto updateDto = new DirectorUpdateDto(1L, "Updated Name");
        DirectorDto updatedDto = new DirectorDto(1L, "Updated Name");

        when(directorService.updateDirector(any(DirectorUpdateDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateDirector_WithoutId_ShouldReturnBadRequest() throws Exception {
        DirectorUpdateDto invalidDto = new DirectorUpdateDto(null, "Updated Name");

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(directorService, never()).updateDirector(any());
    }

    @Test
    void updateDirector_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        DirectorUpdateDto invalidDto = new DirectorUpdateDto(1L, "");

        mockMvc.perform(put("/directors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(directorService, never()).updateDirector(any());
    }

    @Test
    void deleteDirector_ShouldReturnNoContent() throws Exception {
        doNothing().when(directorService).deleteDirectorById(1L);

        mockMvc.perform(delete("/directors/1"))
                .andExpect(status().isNoContent());

        verify(directorService).deleteDirectorById(1L);
    }
}
