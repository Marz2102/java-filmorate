package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MpaController.class)
class MpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MpaService mpaService;

    private MpaDto mpaDto;

    @BeforeEach
    void setUp() {
        mpaDto = new MpaDto(1L, "G");
    }

    @Test
    void getMpaById_ShouldReturnMpa() throws Exception {
        when(mpaService.getMpaById(1L)).thenReturn(mpaDto);

        mockMvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G"));
    }

    @Test
    void getAllMpa_ShouldReturnList() throws Exception {
        List<MpaDto> mpaList = List.of(
                new MpaDto(1L, "G"),
                new MpaDto(2L, "PG"),
                new MpaDto(3L, "PG-13")
        );
        when(mpaService.getAllMpa()).thenReturn(mpaList);

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("G"))
                .andExpect(jsonPath("$[1].name").value("PG"))
                .andExpect(jsonPath("$[2].name").value("PG-13"));
    }

    @Test
    void getMpaById_NotFound_ShouldReturn404() throws Exception {
        when(mpaService.getMpaById(999L)).thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Рейтинг с id - 999 не найден"));

        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound());
    }
}
