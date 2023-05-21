package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testEquals() {
        User user1 = User.builder().email("email1@host.com").name("name").build();
        User user2 = User.builder().email("email1@host.com").name("name").build();
        User user3 = User.builder().email("email1@host.com").name("name").build();
        user3.setId(3L);
        User user4 = User.builder().email("email4@host.com").name("name").build();

        assertEquals(user1, user2);
        assertEquals(user1, user3);
        assertNotEquals(user1, user4);
    }
}