package ru.practicum.shareit.requests.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemRequestDto {

    private Long id;
    private String description;
    private LocalDateTime created;
}