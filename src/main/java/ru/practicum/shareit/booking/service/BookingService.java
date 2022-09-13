package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;

import javax.validation.ValidationException;
import java.util.List;

public interface BookingService {
    Booking add(Long userId, BookingInputDto bookingInputDto) throws ValidationException;

    Booking bookingConfirmation(Long userId, Long bookingId, boolean approved);

    Booking getById(Long userId, Long bookingId);

    List<Booking> getAllBookingByUser(Long userId, String state);

    List<Booking> getAllBookingByOwner(Long userId, String state);
}
