package ru.yandex.practicum.filmorate.model;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Operation {
    ADD,
    UPDATE,
    REMOVE;

    public static Operation from(String operation) {
        return switch (operation) {
            case "ADD" -> ADD;
            case "UPDATE" -> UPDATE;
            case "REMOVE" -> REMOVE;
            default -> {
                log.error("Непредвиденная ошибка конвертации Operation из {}", operation);
                throw new RuntimeException("Ошибка конвертации Operation");
            }
        };
    }
}
