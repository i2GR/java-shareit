package ru.practicum.shareit.common.util.inmemory;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.StorageConflictException;
import ru.practicum.shareit.user.InMemoryUserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryStorageTestOnUserClass {

    InMemoryUserStorage storage;

    @Test
    void createUser() {
        storage = new InMemoryUserStorage();
        User expected = User.builder().email("email@host.com").name("name").build();

        User actual = storage.create(expected).orElseThrow();

        assertNotNull(actual);
        assertEquals(expected, actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    void createUserWithDuplicateData() {
        storage = new InMemoryUserStorage();
        User user = User.builder().email("email@host.com").name("name").build();

        storage.create(user);

        assertThrows(StorageConflictException.class, () -> storage.create(user));
    }

    @Test
    void userIdIncrement() {
        storage = new InMemoryUserStorage();
        User expected1 = User.builder().email("email1@host.com").name("name").build();
        User expected2 = User.builder().email("email2@host.com").name("name").build();

        User actual1 = storage.create(expected1).orElseThrow();
        try {
            storage.create(expected1);
        } catch (StorageConflictException sce) {
            //skip
        }
        User actual2 = storage.create(expected2).orElseThrow();

        assertEquals(1L, actual1.getId());
        assertEquals(2L, actual2.getId());
    }

    @Test
    void updateUser() {
        storage = new InMemoryUserStorage();
        User user = User.builder().email("email@host.com").name("name").build();
        User expected = User.builder().email("email1@host.com").name("name").build();
        user = storage.create(user).orElseThrow();
        expected.setId(user.getId());

        User actual = storage.update(expected).orElseThrow();

        assertNotNull(actual);
        assertEquals(expected, actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    void updateUserWithDuplicateFieldUserEmail() {
        storage = new InMemoryUserStorage();
        User user1 = User.builder().email("first@host.dom").name("name1").build();
        User user2 = User.builder().email("second@host.dom").name("name2").build();
        user1 = storage.create(user1).orElseThrow();
        user2 = storage.create(user2).orElseThrow();
        User user1duplicate = User.builder().email(user2.getEmail()).name(user1.getEmail()).build();
        user1duplicate.setId(user1.getId());

        assertThrows(StorageConflictException.class, () -> storage.update(user1duplicate));
        assertEquals("first@host.dom", storage.readById(1L).orElseThrow().getEmail());
    }

    @Test
    void readAllUsers() {
        storage = new InMemoryUserStorage();
        List<User> empty = storage.readAll();

        User user1 = storage.create(User.builder().email("first@host.dom").name("name1").build()).orElseThrow();
        User user2 = storage.create(User.builder().email("second@host.dom").name("name2").build()).orElseThrow();
        List<User> list = storage.readAll();

        assertEquals(0, empty.size());
        assertEquals(2, list.size());
        assertEquals(user1, list.get(0));
        assertEquals(user2, list.get(1));
    }

    @Test
    void deleteUser() {
        storage = new InMemoryUserStorage();
        List<User> empty = storage.readAll();
        User user1 = storage.create(User.builder().email("first@host.dom").name("name1").build()).orElseThrow();
        List<User> list = storage.readAll();

        User userDeleted = storage.delete(1L).orElseThrow();

        assertEquals(0, empty.size());
        assertEquals(1, list.size());
        assertEquals(0, storage.readAll().size());
        assertEquals(user1, userDeleted);
        assertEquals(user1, list.get(0));
        assertTrue(storage.delete(1L).isEmpty());
    }
}