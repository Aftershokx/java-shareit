package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingOutputDto add(@RequestHeader(USER_HEADER) long userId,
                                @Valid @RequestBody BookingInputDto bookingInputDto) throws ValidationException {
        return BookingMapper.toBookingDto(bookingService.add(userId, bookingInputDto));
    }

    @PatchMapping("/{bookingId}")
    public BookingOutputDto bookingConfirmation(@RequestHeader(USER_HEADER) long userId,
                                                @PathVariable Long bookingId,
                                                @RequestParam(value = "approved") boolean approved) {
        return BookingMapper.toBookingDto(bookingService.bookingConfirmation(userId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingOutputDto getById(@RequestHeader(USER_HEADER) long userId,
                                    @PathVariable Long bookingId) {
        return BookingMapper.toBookingDto(bookingService.getById(userId, bookingId));
    }

    @GetMapping
    public List<BookingOutputDto> getAllBookingByUser(@RequestHeader(USER_HEADER) long userId,
                                                      @RequestParam(value = "state", required = false,
                                                              defaultValue = "ALL") String state) {
        return bookingService.getAllBookingByUser(userId, state)
                .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingOutputDto> getAllBookingByOwner(@RequestHeader(USER_HEADER) long userId,
                                                       @RequestParam(value = "state", required = false,
                                                               defaultValue = "ALL") String state) {
        return bookingService.getAllBookingByOwner(userId, state)
                .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
