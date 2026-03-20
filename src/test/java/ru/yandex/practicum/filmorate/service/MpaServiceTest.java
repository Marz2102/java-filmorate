package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpaServiceTest {

    @Mock
    private MpaStorage mpaStorage;

    @InjectMocks
    private MpaService mpaService;

    private Mpa mpa;
    private MpaDto mpaDto;

    @BeforeEach
    void setUp() {
        mpa = new Mpa(1L, "G");
        mpaDto = new MpaDto(1L, "G");
    }

    @Test
    void getMpaById_ShouldReturnMpa() {
        when(mpaStorage.findById(1L)).thenReturn(Optional.of(mpa));

        MpaDto result = mpaService.getMpaById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("G");

        verify(mpaStorage).findById(1L);
    }

    @Test
    void getMpaById_NotFound_ShouldThrowException() {
        when(mpaStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> mpaService.getMpaById(999L));

        verify(mpaStorage).findById(999L);
    }

    @Test
    void getAllMpa_ShouldReturnList() {
        List<Mpa> mpaList = List.of(
                new Mpa(1L, "G"),
                new Mpa(2L, "PG"),
                new Mpa(3L, "PG-13")
        );
        when(mpaStorage.getAllMpa()).thenReturn(mpaList);

        List<MpaDto> result = mpaService.getAllMpa();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("G");
        assertThat(result.get(1).getName()).isEqualTo("PG");
        assertThat(result.get(2).getName()).isEqualTo("PG-13");

        verify(mpaStorage).getAllMpa();
    }
}
