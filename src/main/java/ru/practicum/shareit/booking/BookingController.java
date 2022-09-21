package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
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
    public BookingResponseDto add(@RequestHeader(USER_HEADER) long userId,
                                  @Valid @RequestBody BookingRequestDto bookingRequestDto) throws ValidationException{
        return BookingMapper.toBookingDto(bookingService.add(userId, bookingRequestDto));
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto bookingConfirmation(@RequestHeader(USER_HEADER) long userId,
                                                  @PathVariable Long bookingId,
                                                  @RequestParam(value = "approved") boolean approved){
        return BookingMapper.toBookingDto(bookingService.bookingConfirmation(userId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader(USER_HEADER) long userId,
                                      @PathVariable Long bookingId){
        return BookingMapper.toBookingDto(bookingService.getById(userId, bookingId));
    }

    @GetMapping
    public List<BookingResponseDto> getAllBookingByUser(@RequestHeader(USER_HEADER) long userId,
                                                        @RequestParam(value = "state", required = false,
                                                                defaultValue = "ALL") String state){
        return bookingService.getAllBookingByUser(userId, state)
                .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllBookingByOwner(@RequestHeader(USER_HEADER) long userId,
                                                         @RequestParam(value = "state", required = false,
                                                                 defaultValue = "ALL") String state){
        return bookingService.getAllBookingByOwner(userId, state)
                .stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
