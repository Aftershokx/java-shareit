package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@JsonTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingDtoJsonTest {

    private final JacksonTester<BookingRequestDto> jsonRequestDto;
    private final JacksonTester<BookingResponseDto> jsonResponseDto;
    private final LocalDateTime start = LocalDateTime.of(2022, 3, 10, 4, 5, 7);
    private final LocalDateTime end = LocalDateTime.of(2022, 3, 11, 4, 5, 9);
    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        bookingRequestDto = BookingRequestDto.builder()
                .id(1L)
                .bookerId(1L)
                .itemId(1L)
                .status(APPROVED)
                .startDate(start)
                .endDate(end)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .booker(new UserDto())
                .id(1L)
                .item(new ItemDto())
                .endDate(end)
                .startDate(start)
                .status(APPROVED)
                .build();
    }

    @Test
    void testBookingRequestDto() throws Exception {
        JsonContent<BookingRequestDto> result = jsonRequestDto.write(bookingRequestDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2022-03-10T04:05:07");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2022-03-11T04:05:09");
    }

    @Test
    void testBookingResponseDto() throws Exception {
        JsonContent<BookingResponseDto> result = jsonResponseDto.write(bookingResponseDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2022-03-10T04:05:07");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2022-03-11T04:05:09");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).doesNotHaveEmptyJsonPathValue("$.booker");
        assertThat(result).doesNotHaveEmptyJsonPathValue("$.item");
    }

}