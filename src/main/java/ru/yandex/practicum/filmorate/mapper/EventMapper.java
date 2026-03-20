package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.event.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static EventDto mapEventToEventDto(Event event) {
        EventDto eventDto = new EventDto();

        eventDto.setUserId(event.getUserId());
        eventDto.setEventId(event.getEventId());
        eventDto.setEntityId(event.getEntityId());
        eventDto.setEventType(event.getEventType().toString());
        eventDto.setOperation(event.getOperation().toString());
        eventDto.setTimestamp(event.getCreationDateTime().getTime());

        return eventDto;
    }

}
