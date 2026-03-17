package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MpaMapper {

    public static Mpa mapMpaDtoToMpa(MpaDto mpaDto) {
        Mpa mpa = new Mpa();

        mpa.setId(mpaDto.getId());
        mpa.setName(mpaDto.getName());

        return mpa;
    }

    public static MpaDto mapMpaToMpaDto(Mpa mpa) {
        MpaDto mpaDto = new MpaDto();

        mpaDto.setId(mpa.getId());
        mpaDto.setName(mpa.getName());

        return mpaDto;
    }
}
