package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.comment.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ItemDtoForBooking {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private List<CommentDto> comments;
    private BookingDto lastBooking;
    private BookingDto nextBooking;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BookingDto {
        long id;
        long bookerId;
        @JsonProperty("start")
        LocalDateTime start;
        @JsonProperty("end")
        LocalDateTime end;
    }

}
