package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
@Slf4j
public class MpaService {

    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(@Qualifier("MpaDao") final MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public MpaDto getMpaById(Long id) {
        return mpaStorage.findById(id)
                .map(MpaMapper::mapMpaToMpaDto)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id - " + id + " не найден"));
    }

    public List<MpaDto> getAllMpa() {
        return mpaStorage.getAllMpa().stream()
                .map(MpaMapper::mapMpaToMpaDto)
                .toList();
    }
}
