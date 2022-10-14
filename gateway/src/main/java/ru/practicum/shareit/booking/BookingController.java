package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@Validated
@RequestMapping(path = "/bookings")
public class BookingController {

    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @Autowired
    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_HEADER) long userId,
                                      @Valid @RequestBody BookingRequestDto bookingRequestDto) throws ValidationException {
        return bookingClient.add(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> bookingConfirmation(@RequestHeader(USER_HEADER) long userId,
                                                      @PathVariable Long bookingId,
                                                      @RequestParam(value = "approved") boolean approved) {
        return bookingClient.bookingConfirmation(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_HEADER) long userId,
                                          @PathVariable Long bookingId) {
        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader(USER_HEADER) long userId,
                                          @RequestParam(value = "state", required = false,
                                                  defaultValue = "ALL") String state,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                          @RequestParam(defaultValue = "20") @Positive int size) {
        return bookingClient.getAll(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingByOwner(@RequestHeader(USER_HEADER) long userId,
                                                       @RequestParam(value = "state", required = false,
                                                               defaultValue = "ALL") String state,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                       @RequestParam(defaultValue = "20") @Positive int size) {
        return bookingClient.getAllBookingByOwner(userId, state, from, size);
    }
}
