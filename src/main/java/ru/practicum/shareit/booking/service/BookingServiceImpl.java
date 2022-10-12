package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.StateStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking add(Long userId, BookingRequestDto bookingRequestDto) throws ValidationException {
        checkInputBookingDto(userId, bookingRequestDto);
        bookingRequestDto.setStatus(BookingStatus.WAITING);
        checkItemAvailable(itemRepository.findById(bookingRequestDto.getItemId()).orElseThrow(() ->
                new NoSuchElementException("Item By id not found")));
        return bookingRepository.save(BookingMapper.toBooking(bookingRequestDto, userRepository.findById(userId).orElseThrow(() ->
                        new NoSuchElementException("UserNotFound By id not found")),
                itemRepository.findById(bookingRequestDto.getItemId()).orElseThrow(() ->
                        new NoSuchElementException("Item By id not found"))));
    }

    @Override
    public Booking bookingConfirmation(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new ItemNotAvailableException("Cant change approved bookings");
        }
        BookingStatus status;
        if (approved) {
            status = BookingStatus.APPROVED;
        } else {
            status = BookingStatus.REJECTED;
        }
        if (checkOwner(userId, booking)) {
            booking.setStatus(status);
        } else {
            throw new NoSuchElementException("User does not own this item");
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (checkOwner(userId, booking) || booking.getBooker().getId() == userId) {
            return booking;
        } else {
            throw new NoSuchElementException("User does not own this item");
        }
    }

    @Override
    public List<Booking> getAll(Long userId, String state, int from, int size) {
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("User By id " + userId + " not found"));

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());

        switch (checkStatus(state).orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state))) {
            case CURRENT:
                return new ArrayList<>(bookingRepository
                        .findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(),
                                LocalDateTime.now(), pageable)
                        .orElseThrow(() -> new NoSuchElementException("Current bookings for user "
                                + userId + " not found")));
            case PAST:
                return new ArrayList<>(bookingRepository.findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(userId,
                                LocalDateTime.now(), pageable)
                        .orElseThrow(() -> new NoSuchElementException("Past bookings for user "
                                + userId + " not found")));
            case FUTURE:
                return new ArrayList<>(bookingRepository.findAllByBooker_IdAndStartIsAfterOrderByStartDesc(userId,
                                LocalDateTime.now(), pageable)
                        .orElseThrow(() -> new NoSuchElementException("Future bookings for user "
                                + userId + " not found")));
            case WAITING:
                return new ArrayList<>(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId,
                                BookingStatus.WAITING, pageable)
                        .orElseThrow(() -> new NoSuchElementException("Waiting bookings for user  " +
                                userId + " not found")));
            case REJECTED:
                return new ArrayList<>(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId,
                                BookingStatus.REJECTED, pageable)
                        .orElseThrow(() -> new NoSuchElementException("Rejected bookings for user "
                                + userId + " not found")));
            default:
                return new ArrayList<>(bookingRepository.findAllByBooker_IdOrderByStartDesc(userId, pageable)
                        .orElseThrow(() -> new NoSuchElementException("Bookings for user "
                                + userId + " not found")));
        }
    }

    @Override
    public List<Booking> getAllBookingByOwner(Long userId, String state, int from, int size) {
        List<Item> items = itemRepository.findByOwner(userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("UserNotFound By id not found")));
        if (items.size() == 0) {
            throw new NoSuchElementException("User " + userId + " does not own any items");
        }
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("start").descending());
        List<Booking> bookings = new ArrayList<>();

        switch (checkStatus(state).orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state))) {
            case CURRENT:
                items.forEach(item -> bookings.addAll(bookingRepository
                        .findAllByItem_IdAndStartBeforeAndEndAfterOrderByStartDesc(item.getId(), LocalDateTime.now(),
                                LocalDateTime.now(), pageable)));
                if (bookings.size() != 0) {
                    return bookings;
                } else {
                    throw new NoSuchElementException("Current bookings for user "
                            + userId + " not found");
                }
            case PAST:
                items.forEach(item -> bookings.addAll(bookingRepository
                        .findAllByItem_IdAndEndIsBeforeOrderByStartDesc(item.getId(), LocalDateTime.now(), pageable)));
                if (bookings.size() != 0) {
                    return bookings;
                } else {
                    throw new NoSuchElementException("Past bookings for user "
                            + userId + " not found");
                }
            case FUTURE:
                items.forEach(item -> bookings.addAll(bookingRepository
                        .findAllByItem_IdAndStartIsAfterOrderByStartDesc(item.getId(), LocalDateTime.now(), pageable)));
                if (bookings.size() != 0) {
                    return bookings;
                } else {
                    throw new NoSuchElementException("Future bookings for user "
                            + userId + " not found");
                }
            case WAITING:
                items.forEach(item -> bookings.addAll(bookingRepository
                        .findAllByItem_IdAndStatusOrderByStartDesc(item.getId(), BookingStatus.WAITING, pageable)));
                if (bookings.size() != 0) {
                    return bookings;
                } else {
                    throw new NoSuchElementException("Waiting bookings for user  " +
                            userId + " not found");
                }
            case REJECTED:
                items.forEach(item -> bookings.addAll(bookingRepository
                        .findAllByItem_IdAndStatusOrderByStartDesc(item.getId(), BookingStatus.REJECTED, pageable)));
                if (bookings.size() != 0) {
                    return bookings;
                } else {
                    throw new NoSuchElementException("Rejected bookings for user "
                            + userId + " not found");
                }
            default:
                items.forEach(item -> bookings.addAll(bookingRepository
                        .findAllByItem_IdOrderByStartDesc(item.getId(), pageable)));
                if (bookings.size() != 0) {
                    return bookings;
                } else {
                    throw new NoSuchElementException("Bookings for user "
                            + userId + " not found");
                }
        }
    }

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

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking with id: "
                        + bookingId + " not exist"));
    }

    private boolean checkOwner(Long userId, Booking booking) {
        return booking.getItem().getOwner().getId() == userId;
    }

    private void checkItemAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Item " + item.getId() + " unreliable");
        }
    }

    private void checkInputBookingDto(long userId, BookingRequestDto bookingRequestDto) throws ValidationException {
        if (bookingRequestDto.getStartDate().isAfter(bookingRequestDto.getEndDate())) {
            throw new ItemNotAvailableException("Booking start time cannot be later then end of booking");
        }
        if (bookingRequestDto.getStartDate() == bookingRequestDto.getEndDate()) {
            throw new ItemNotAvailableException("Booking start time cannot be equals with end date");
        }
        if (userId == itemRepository.findById(bookingRequestDto.getItemId()).orElseThrow(() ->
                new NoSuchElementException("Item By id not found")).getOwner().getId()) {
            throw new NoSuchElementException("user " + userId + " cannot book his own item "
                    + bookingRequestDto.getItemId());
        }
    }

    private Optional<StateStatus> checkStatus(String state) {
        if (state.isBlank()) {
            return Optional.of(StateStatus.ALL);
        }
        switch (state.toUpperCase()) {
            case "CURRENT":
                return Optional.of(StateStatus.CURRENT);
            case "PAST":
                return Optional.of(StateStatus.PAST);
            case "FUTURE":
                return Optional.of(StateStatus.FUTURE);
            case "WAITING":
                return Optional.of(StateStatus.WAITING);
            case "REJECTED":
                return Optional.of(StateStatus.REJECTED);
            case "ALL":
                return Optional.of(StateStatus.ALL);
        }
        return Optional.empty();
    }
}
