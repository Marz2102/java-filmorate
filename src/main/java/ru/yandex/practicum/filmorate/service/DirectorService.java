package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.director.DirectorUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
public class DirectorService {

    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(@Qualifier("DirectorDao") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<DirectorDto> getDirectors() {
        return directorStorage.getDirectors().stream()
                .map(DirectorMapper::mapDirectorToDirectorDto)
                .toList();
    }

    public DirectorDto getDirectorById(Long id) {
        return DirectorMapper.mapDirectorToDirectorDto(getDirectorOrThrow(id));
    }

    public void deleteDirectorById(Long id) {
        getDirectorOrThrow(id);

        directorStorage.deleteDirector(id);
    }

    public DirectorDto addDirector(DirectorDto directorDto) {
        Director director = DirectorMapper.mapDirectorDtoToDirector(directorDto);

        director = directorStorage.addDirector(director);

        return DirectorMapper.mapDirectorToDirectorDto(director);
    }

    public DirectorDto updateDirector(DirectorUpdateDto directorUpdateDto) {
        Director director = getDirectorOrThrow(directorUpdateDto.getId());

        Director updatedDirector = DirectorMapper.updateDirectorFields(directorUpdateDto, director);

        return DirectorMapper.mapDirectorToDirectorDto(directorStorage.updateDirector(updatedDirector));
    }

    private Director getDirectorOrThrow(Long id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Директор с id - " + id + " не найден"));
    }
}
