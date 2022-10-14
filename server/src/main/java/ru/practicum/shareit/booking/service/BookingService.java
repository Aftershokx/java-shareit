package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking add(Long userId, BookingRequestDto bookingRequestDto);

    Booking bookingConfirmation(Long userId, Long bookingId, boolean approved);

    Booking getById(Long userId, Long bookingId);

    List<Booking> getAll(Long userId, String state, int from, int size);

    List<Booking> getAllBookingByOwner(Long userId, String state, int from, int size);

    Optional<Booking> getLastBooking(long itemId);

    Optional<Booking> getNextBooking(long itemId);

    boolean checkBooking(long userId, long itemId, BookingStatus status);
}
