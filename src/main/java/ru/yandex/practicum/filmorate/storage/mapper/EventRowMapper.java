package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Event event = new Event();

        event.setEventId(resultSet.getLong("id"));
        event.setUserId(resultSet.getLong("user_id"));
        event.setEntityId(resultSet.getLong("entity_id"));
        event.setEventType(EventType.from(resultSet.getString("event_type")));
        event.setOperation(Operation.from(resultSet.getString("operation")));
        event.setCreationDateTime(resultSet.getTimestamp("created_at"));

        return event;
    }
}
