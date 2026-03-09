package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getFilms() {
        log.info("Получен список всех фильмов");
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(generateNextId());
        films.put(film.getId(), film);

        log.info("Успешно добавлен новый фильм с id = {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);

        log.info("Данные фильма с id = {} успешно обновлены", film.getId());
        return film;
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        film.addLike(userId);

        log.info("Пользователь с id - {} поставил лайк фильму с id - {}", userId, filmId);
        return film;
    }

    @Override
    public Film deleteLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        film.deleteLike(userId);

        log.info("Пользователь с id - {} удалил лайк у фильма с id - {}", userId, filmId);
        return film;
    }

    @Override
    public List<Film> getMostLikedFilms(int count) {
        log.info("Получен список {} самых популярных фильмов", count);
        return films.values()
                .stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikedUsers().size(), f1.getLikedUsers().size()))
                .limit(count)
                .toList();
    }

    @Override
    public Long generateNextId() {
        long currentId = films
                .keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L) + 1;

        log.debug("Сгенерирован новый id - {}", currentId);
        return currentId;
    }
}
