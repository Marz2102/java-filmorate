package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
@Slf4j
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(@Qualifier("GenreDao") final GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public GenreDto getGenreById(Long id) {
        return genreStorage.findById(id)
                .map(GenreMapper::mapGenreToGenreDto)
                .orElseThrow(() -> new NotFoundException("Жанр с id - " + id + " не найден"));
    }

    public List<GenreDto> getGenres() {
        return genreStorage.getGenres().stream()
                .map(GenreMapper::mapGenreToGenreDto)
                .toList();
    }
}
