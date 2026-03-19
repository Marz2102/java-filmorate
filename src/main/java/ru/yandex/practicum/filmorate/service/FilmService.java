package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final UserService userService;
    private final FilmStorage filmStorage;
    private final GenreService genreService;
    private final MpaService mpaService;
    private final DirectorService directorService;

    public FilmService(UserService userService, @Qualifier("FilmDao") final FilmStorage filmStorage,
                       GenreService genreService, MpaService mpaService, DirectorService directorService) {
        this.userService = userService;
        this.filmStorage = filmStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.directorService = directorService;
    }

    public FilmDto getFilmById(Long id) {
        return FilmMapper.mapFilmToFilmDto(filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id - " + id + " не найден")));
    }

    public List<FilmDto> getFilms() {
        return filmStorage.getFilms()
                .stream()
                .map(FilmMapper::mapFilmToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto addFilm(FilmCreateDto filmCreateDto) {
        checkReleaseDate(filmCreateDto.getReleaseDate());

        Film film = FilmMapper.mapFilmCreateDtoToFilm(filmCreateDto);

        Set<Genre> genres = filmCreateDto.getGenreIds()
                .stream()
                .map(genreService::getGenreById)
                .map(GenreMapper::mapGenreDtoToGenre)
                .collect(Collectors.toSet());
        film.setGenres(genres);

        Set<Director> directors = filmCreateDto.getDirectorIds()
                .stream()
                .map(directorService::getDirectorById)
                .map(DirectorMapper::mapDirectorDtoToDirector)
                .collect(Collectors.toSet());
        film.setDirectors(directors);

        if (filmCreateDto.getMpaId() != null) {
            Mpa mpa = MpaMapper.mapMpaDtoToMpa(mpaService.getMpaById(filmCreateDto.getMpaId()));
            film.setMpa(mpa);
        }

        film = filmStorage.addFilm(film);

        log.info("Валидация запроса прошла успешно");

        return FilmMapper.mapFilmToFilmDto(film);
    }

    public List<FilmDto> getAllFilmsByDirectorId(Long directorId, String sortParam) {
        List<Film> films = filmStorage.getFilmsByDirectorId(directorId, sortParam);

        return films.stream()
                .map(FilmMapper::mapFilmToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto updateFilm(FilmUpdateDto filmUpdateDto) {
        checkReleaseDate(filmUpdateDto.getReleaseDate());

        log.info("Валидация запроса прошла успешно");

        Film updatedFilm = filmStorage.findById(filmUpdateDto.getId())
                .map(film -> FilmMapper.updateFilmField(filmUpdateDto, film))
                .orElseThrow(() -> new NotFoundException("Фильм с id - " + filmUpdateDto.getId() + " не найден"));

        return FilmMapper.mapFilmToFilmDto(filmStorage.updateFilm(updatedFilm));
    }

    public FilmDto addLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);
        return FilmMapper.mapFilmToFilmDto(filmStorage.addLike(filmId, userId));
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);
        return FilmMapper.mapFilmToFilmDto(filmStorage.deleteLike(filmId, userId));
    }

    public List<FilmDto> getMostLikedFilms(int count) {
        if (count <= 0) {
            log.info("Параметр count = {}", count);
            throw new ValidationException("Укажите положительный параметр count");
        }

        return filmStorage.getMostLikedFilms(count)
                .stream()
                .map(FilmMapper::mapFilmToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getCommonFilms(Long userId, Long friendId) {
        userService.getUserById(userId);
        userService.getUserById(friendId);

        return filmStorage.getCommonFilms(userId, friendId)
                .stream()
                .map(FilmMapper::mapFilmToFilmDto)
                .collect(Collectors.toList());
    }

    private void checkToFindByIds(Long filmId, Long userId) {
        getFilmById(filmId);
        userService.getUserById(userId);
    }

    private void checkReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null && releaseDate.isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Проблема с полем 'Дата релиза'");
            throw new ValidationException("Дата релиза не может быть раньше " + CINEMA_BIRTHDAY);
        }
    }
}
