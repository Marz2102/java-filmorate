package ru.yandex.practicum.filmorate.model;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum EventType {
    LIKE,
    REVIEW,
    FRIEND;

    public static EventType from(String type) {
        return switch (type) {
            case "LIKE" -> LIKE;
            case "REVIEW" -> REVIEW;
            case "FRIEND" -> FRIEND;
            default -> {
                log.error("Непредвиденная ошибка конвертации EventType из {}", type);
                throw new RuntimeException("Ошибка конвертации EventType");
            }
        };
    }
}
