package ru.yandex.practicum.filmorate.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateDto;
import ru.yandex.practicum.filmorate.model.Director;

import static org.assertj.core.api.Assertions.assertThat;

class DirectorMapperTest {

    @Test
    void mapDirectorToDirectorDto_ShouldMapCorrectly() {
        Director director = new Director(1L, "Christopher Nolan");

        DirectorDto dto = DirectorMapper.mapDirectorToDirectorDto(director);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Christopher Nolan");
    }

    @Test
    void mapDirectorDtoToDirector_ShouldMapCorrectly() {
        DirectorDto dto = new DirectorDto(1L, "Christopher Nolan");

        Director director = DirectorMapper.mapDirectorDtoToDirector(dto);

        assertThat(director.getId()).isEqualTo(1L);
        assertThat(director.getName()).isEqualTo("Christopher Nolan");
    }

    @Test
    void updateDirectorFields_ShouldUpdateName() {
        Director director = new Director(1L, "Old Name");
        DirectorUpdateDto updateDto = new DirectorUpdateDto(1L, "New Name");

        Director updated = DirectorMapper.updateDirectorFields(updateDto, director);

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getId()).isEqualTo(1L);
    }

    @Test
    void updateDirectorFields_WithNullName_ShouldNotUpdate() {
        Director director = new Director(1L, "Old Name");
        DirectorUpdateDto updateDto = new DirectorUpdateDto(1L, null);

        Director updated = DirectorMapper.updateDirectorFields(updateDto, director);

        assertThat(updated.getName()).isEqualTo("Old Name");
    }
}
