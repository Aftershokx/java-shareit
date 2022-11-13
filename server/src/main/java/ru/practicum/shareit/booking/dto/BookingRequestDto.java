package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BookingRequestDto {
    private long id;
    private Long bookerId;
    private Long itemId;
    @JsonProperty("start")
    private LocalDateTime startDate;
    @JsonProperty("end")
    private LocalDateTime endDate;
    private BookingStatus status;

}