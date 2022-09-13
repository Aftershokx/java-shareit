package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BookingServiceForItemImpl implements BookingServiceForItem {
    private final BookingRepository bookingRepository;

    @Override
    public Optional<Booking> getLastBooking(long itemId) {
        return bookingRepository.findFirstBookingByItem_IdAndEndIsBeforeOrderByEndDesc(itemId,
                LocalDateTime.now());
    }

    @Override
    public Optional<Booking> getNextBooking(long itemId) {
        return bookingRepository.findFirstBookingByItem_IdAndStartIsAfterOrderByStart(itemId,
                LocalDateTime.now());
    }

    @Override
    public boolean checkBooking(long userId, long itemId, BookingStatus status) {
        return bookingRepository.existsBookingByBooker_IdAndItem_IdAndStatusEqualsAndEndIsBefore(userId,
                itemId, status, LocalDateTime.now());
    }

}