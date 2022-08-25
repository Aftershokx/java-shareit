package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDate;

@Data
@Builder
public class BookingDto {
    private LocalDate start;
    private LocalDate end;
    private Item item;
    private User booker;
    private BookingStatus status;

    @Data
    public static class Item {
        private Long id;
    }

    @Data
    public static class User {
        private Long id;
    }
}