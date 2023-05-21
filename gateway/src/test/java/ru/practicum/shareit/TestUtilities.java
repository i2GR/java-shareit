package ru.practicum.shareit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class TestUtilities {

    public static <T> ResponseEntity<Object> getOkResponse(T obj) {
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }
}