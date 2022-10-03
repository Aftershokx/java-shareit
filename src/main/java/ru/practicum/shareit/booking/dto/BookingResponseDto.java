package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BookingResponseDto {
    private long id;
    private UserDto booker;
    private ItemDto item;
    @JsonProperty("start")
    private LocalDateTime startDate;
    @JsonProperty("end")
    private LocalDateTime endDate;
    private BookingStatus status;
}