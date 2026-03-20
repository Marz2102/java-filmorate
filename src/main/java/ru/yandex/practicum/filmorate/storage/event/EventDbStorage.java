package ru.yandex.practicum.filmorate.storage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.mapper.EventRowMapper;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Repository("EventDao")
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    public EventDbStorage(DataSource dataSource, EventRowMapper eventRowMapper) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.eventRowMapper = eventRowMapper;
    }

    @Override
    public List<Event> getEvents(Long userId) {
        String query = "SELECT * FROM events WHERE user_id = ?";

        return jdbc.query(query, eventRowMapper, userId);
    }

    public void addEvent(long userId, long entityId, EventType eventType, Operation operation) {
        String insertQuery = "INSERT INTO events (user_id, entity_id, event_type, operation) VALUES (?, ?, ?, ?)";

        int rowsAdded = jdbc.update(insertQuery, userId, entityId, eventType.toString(), operation.toString());

        if (rowsAdded == 0) {
            log.error("""
                            Ошибка БД при добавлении строки в Events. Передаваемые параметры:
                            user_id: {}
                            entity_id: {}
                            event_type: {}
                            operation: {}
                            """,
                    userId, entityId, eventType, operation);
        }
    }
}
