package ru.yandex.practicum.filmorate.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    private Long eventId;
    private Long userId;
    private Long entityId;
    private String eventType;
    private String operation;
    private Long timestamp;
}
