package ru.practicum.shareit.common.util.inmemory;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.inmemory.IdService;

import static org.junit.jupiter.api.Assertions.*;

class IdServiceTest {

    @Test
    void getNewId() {
        IdService idService = new IdService();
        User entity = User.builder().email("mail@host.dom").name("name").build();
        Long newId = idService.getNewId(entity);
        Long sameId = idService.getNewId(entity);

        assertEquals(1L, newId);
        assertEquals(1L, sameId);
    }

    @Test
    void updateEntityWithId() {
        IdService idService = new IdService();
        User entity = User.builder().email("mail@host.dom").name("name").build();
        Long expected = idService.getNewId(entity);

        idService.updateEntityWithId(entity, expected);
        Long actual = entity.getId();

        assertEquals(expected, actual);
    }
}