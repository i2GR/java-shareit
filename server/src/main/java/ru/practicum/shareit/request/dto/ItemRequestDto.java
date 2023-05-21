package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * DTO для класса ItemRequest <p>
 * ТЗ-13 <p>
 * @implNote expect SP-15 specs for more details
 */
@Getter
@Builder
@EqualsAndHashCode(exclude = {"id"})
public class ItemRequestDto {

    private Long id;

    private String description;
}