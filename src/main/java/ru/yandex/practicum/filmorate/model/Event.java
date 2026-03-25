package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Long eventId;
    private Long userId;
    private Long entityId;
    private EventType eventType;
    private Operation operation;
    private Timestamp creationDateTime;
}
