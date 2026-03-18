package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateDto;
import ru.yandex.practicum.filmorate.model.Director;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectorMapper {

    public static DirectorDto mapDirectorToDirectorDto(Director director) {
        DirectorDto directorDto = new DirectorDto();

        directorDto.setId(director.getId());
        directorDto.setName(director.getName());

        return directorDto;
    }

    public static Director mapDirectorDtoToDirector(DirectorDto directorDto) {
        Director director = new Director();

        director.setId(directorDto.getId());
        director.setName(directorDto.getName());

        return director;
    }

    public static Director updateDirectorFields(DirectorUpdateDto directorUpdateDto, Director director) {
        if (directorUpdateDto.getName() != null) {
            director.setName(directorUpdateDto.getName());
            log.debug("Обновили имя режиссёра - {}", director.getName());
        }

        return director;
    }
}
