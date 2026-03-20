package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MpaDbStorageTest {

    @Autowired
    @Qualifier("MpaDao")
    private MpaStorage mpaStorage;

    @Test
    void findById_ShouldReturnMpa() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(3L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("name", "PG-13")
                );
    }

    @Test
    void getAllMpa_ShouldReturnAllRatings() {
        List<Mpa> mpaList = mpaStorage.getAllMpa();

        assertEquals(5, mpaList.size());
        assertEquals("G", mpaList.get(0).getName());
        assertEquals("PG", mpaList.get(1).getName());
        assertEquals("PG-13", mpaList.get(2).getName());
        assertEquals("R", mpaList.get(3).getName());
        assertEquals("NC-17", mpaList.get(4).getName());
    }
}
