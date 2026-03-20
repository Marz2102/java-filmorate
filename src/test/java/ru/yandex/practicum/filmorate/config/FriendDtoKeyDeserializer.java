package ru.yandex.practicum.filmorate.config;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import ru.yandex.practicum.filmorate.dto.user.FriendDto;

import java.io.IOException;

public class FriendDtoKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        FriendDto friendDto = new FriendDto();
        friendDto.setId(Long.parseLong(key));
        return friendDto;
    }
}