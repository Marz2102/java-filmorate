package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.FilmUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
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
    private final EventStorage eventStorage;

    public FilmService(UserService userService, @Qualifier("FilmDao") final FilmStorage filmStorage,
                       @Qualifier("EventDao") final EventStorage eventStorage, GenreService genreService,
                       MpaService mpaService, DirectorService directorService) {
        this.userService = userService;
        this.filmStorage = filmStorage;
        this.genreService = genreService;
        this.mpaService = mpaService;
        this.directorService = directorService;
        this.eventStorage = eventStorage;
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
        directorService.getDirectorById(directorId);
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

    public FilmDto addMark(Long filmId, Long userId, Integer mark) {
        checkToFindByIds(filmId, userId);

        Film addedMark = filmStorage.addMark(filmId, userId, mark);
        eventStorage.addEvent(userId, filmId, EventType.LIKE, Operation.ADD);

        return FilmMapper.mapFilmToFilmDto(addedMark);
    }

    public FilmDto addLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);

        Film addedLike = filmStorage.addLike(filmId, userId);
        eventStorage.addEvent(userId, filmId, EventType.LIKE, Operation.ADD);

        return FilmMapper.mapFilmToFilmDto(addedLike);
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        checkToFindByIds(filmId, userId);

        Film removedLike = filmStorage.deleteLike(filmId, userId);
        eventStorage.addEvent(userId, filmId, EventType.LIKE, Operation.REMOVE);

        return FilmMapper.mapFilmToFilmDto(removedLike);
    }

    public List<FilmDto> getMostRatedFilms(int count) {
        return getMostRatedFilms(count, null, null);
    }

    public List<FilmDto> getMostRatedFilms(int count, Long genreId, Integer year) {
        return filmStorage.getMostRatedFilms(count, genreId, year)
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

    public void deleteFilm(Long filmId) {
        log.info("Вызван метод удаления фильма с id {}", filmId);

        getFilmById(filmId);

        filmStorage.deleteFilm(filmId);
        log.info("Фильм с id {} успешно удален", filmId);
    }

    public List<FilmDto> searchFilms(String query, String queryParam) {
        if (query == null || query.isBlank()) {
            log.warn("Не указан текст для поиска");
            throw new ValidationException("Укажите текст для поиска");
        }

        if (queryParam == null || queryParam.isBlank()) {
            log.warn("Не указан параметр для поиска фильмов");
            throw new ValidationException("Укажите параметр director или title для поиска фильмов");
        }

        for (String param : queryParam.split(",")) {
            param = param.trim();
            if (!"director".equals(param) && !"title".equals(param)) {
                log.warn("Не указан параметр для поиска фильмов");
                throw new ValidationException("Параметр для поиска фильмов может содержать только director или title");
            }
        }

        return filmStorage.searchFilms(query, queryParam)
                .stream()
                .map(FilmMapper::mapFilmToFilmDto)
                .toList();
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