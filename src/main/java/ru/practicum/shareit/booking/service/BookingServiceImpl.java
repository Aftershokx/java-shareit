package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.StateStatus;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.exception.WrongDateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking add(Long userId, BookingInputDto bookingInputDto) throws ValidationException {
        checkInputBookingDto(userId, bookingInputDto);
        bookingInputDto.setStatus(BookingStatus.WAITING);
        checkItemAvailable(itemRepository.findById(bookingInputDto.getItemId()).orElseThrow(() ->
                new NoSuchElementException("Item By id not found")));
        return bookingRepository.save(BookingMapper.toBooking(bookingInputDto, userRepository.findById(userId).orElseThrow(() ->
                        new NoSuchElementException("UserNotFound By id not found")),
                itemRepository.findById(bookingInputDto.getItemId()).orElseThrow(() ->
                        new NoSuchElementException("Item By id not found"))));
    }

    @Override
    public Booking bookingConfirmation(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBooking(bookingId);
        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new ItemNotAvailableException("Нельзя изменить статус одобренного бронирования");
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
            throw new NoSuchElementException("Пользователь не является владельцем вещи");
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (checkOwner(userId, booking) || booking.getBooker().getId() == userId) {
            return booking;
        } else {
            throw new NoSuchElementException("Ошибка доступа, пользователь не является" +
                    " владельцем или арендатором вещи");
        }
    }

    @Override
    public List<Booking> getAllBookingByUser(Long userId, String state) {
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("User By id + " + userId + " not found"));
        if (state.equals("CURRENT") || state.equals("PAST") || state.equals("FUTURE")
                || state.equals("WAITING") || state.equals("REJECTED") || state.equals("ALL") || state.isBlank()) {
            switch (StateStatus.valueOf(state)) {
                case CURRENT:
                    return new ArrayList<>(bookingRepository
                            .findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(),
                                    LocalDateTime.now())
                            .orElseThrow(() -> new NoSuchElementException("Текущих бронирований для пользователя "
                                    + userId + " не найдено")));
                case PAST:
                    return new ArrayList<>(bookingRepository.findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(userId,
                                    LocalDateTime.now())
                            .orElseThrow(() -> new NoSuchElementException("Завершенных бронирований для пользователя "
                                    + userId + " не найдено")));
                case FUTURE:
                    return new ArrayList<>(bookingRepository.findAllByBooker_IdAndStartIsAfterOrderByStartDesc(userId,
                                    LocalDateTime.now())
                            .orElseThrow(() -> new NoSuchElementException("Будущих бронирований для пользователя "
                                    + userId + " не найдено")));
                case WAITING:
                    return new ArrayList<>(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId,
                                    BookingStatus.WAITING)
                            .orElseThrow(() -> new NoSuchElementException("Ожидающих подтверждения бронирований " +
                                    "для пользователя " + userId + " не найдено")));
                case REJECTED:
                    return new ArrayList<>(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(userId,
                                    BookingStatus.REJECTED)
                            .orElseThrow(() -> new NoSuchElementException("Отклоненных бронирований для пользователя "
                                    + userId + " не найдено")));
                default:
                    return new ArrayList<>(bookingRepository.findAllByBooker_IdOrderByStartDesc(userId)
                            .orElseThrow(() -> new NoSuchElementException("Бронирований для пользователя "
                                    + userId + " не найдено")));
            }
        } else throw new UnsupportedStatusException("Unknown state: " + state);
    }

    @Override
    public List<Booking> getAllBookingByOwner(Long userId, String state) {
        List<Item> items = itemRepository.findByOwner(userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("UserNotFound By id not found")));
        if (items.size() == 0) {
            throw new NoSuchElementException("У данного пользователя " + userId + " нет вещей");
        }
        List<Booking> bookings = new ArrayList<>();

        if (state.equals("CURRENT") || state.equals("PAST") || state.equals("FUTURE")
                || state.equals("WAITING") || state.equals("REJECTED") || state.equals("ALL") || state.isBlank()) {
            switch (StateStatus.valueOf(state)) {
                case CURRENT:
                    items.forEach(item -> bookings.addAll(bookingRepository
                            .findAllByItem_IdAndStartBeforeAndEndAfterOrderByStartDesc(item.getId(), LocalDateTime.now(),
                                    LocalDateTime.now())));
                    if (bookings.size() != 0) {
                        return bookings;
                    } else {
                        throw new NoSuchElementException("Текущих бронирований для вещей пользователя "
                                + userId + " не найдено");
                    }
                case PAST:
                    items.forEach(item -> bookings.addAll(bookingRepository
                            .findAllByItem_IdAndEndIsBeforeOrderByStartDesc(item.getId(), LocalDateTime.now())));
                    if (bookings.size() != 0) {
                        return bookings;
                    } else {
                        throw new NoSuchElementException("Прошедших бронирований для вещей пользователя "
                                + userId + " не найдено");
                    }
                case FUTURE:
                    items.forEach(item -> bookings.addAll(bookingRepository
                            .findAllByItem_IdAndStartIsAfterOrderByStartDesc(item.getId(), LocalDateTime.now())));
                    if (bookings.size() != 0) {
                        return bookings;
                    } else {
                        throw new NoSuchElementException("Прошедших бронирований для вещей пользователя "
                                + userId + " не найдено");
                    }
                case WAITING:
                    items.forEach(item -> bookings.addAll(bookingRepository
                            .findAllByItem_IdAndStatusOrderByStartDesc(item.getId(), BookingStatus.WAITING)));
                    if (bookings.size() != 0) {
                        return bookings;
                    } else {
                        throw new NoSuchElementException("Прошедших бронирований для вещей пользователя "
                                + userId + " не найдено");
                    }
                case REJECTED:
                    items.forEach(item -> bookings.addAll(bookingRepository
                            .findAllByItem_IdAndStatusOrderByStartDesc(item.getId(), BookingStatus.REJECTED)));
                    if (bookings.size() != 0) {
                        return bookings;
                    } else {
                        throw new NoSuchElementException("Прошедших бронирований для вещей пользователя "
                                + userId + " не найдено");
                    }
                default:
                    items.forEach(item -> bookings.addAll(bookingRepository
                            .findAllByItem_IdOrderByStartDesc(item.getId())));
                    if (bookings.size() != 0) {
                        return bookings;
                    } else {
                        throw new NoSuchElementException("Прошедших бронирований для вещей пользователя "
                                + userId + " не найдено");
                    }
            }
        } else throw new UnsupportedStatusException("Unknown state: " + state);
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирования с таким id: "
                        + bookingId + " не существует"));
    }

    private boolean checkOwner(Long userId, Booking booking) {
        return booking.getItem().getOwner().getId() == userId;
    }

    private void checkItemAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь " + item.getId() + " недоступна");
        }
    }

    private void checkInputBookingDto(long userId, BookingInputDto bookingInputDto) throws ValidationException {
        if (bookingInputDto.getStartDate().isAfter(bookingInputDto.getEndDate())) {
            throw new WrongDateException("Время начала бронирования не может быть позже времени" +
                    " окончания бронирования");
        }
        if (bookingInputDto.getStartDate() == bookingInputDto.getEndDate()) {
            throw new WrongDateException("Время начала бронирования не может быть равно времени" +
                    " окончания бронирования");
        }
        if (userId == itemRepository.findById(bookingInputDto.getItemId()).orElseThrow(() ->
                new NoSuchElementException("Item By id not found")).getOwner().getId()) {
            throw new NoSuchElementException("Пользователь " + userId + " не может забронировать свою вещь "
                    + bookingInputDto.getItemId());
        }
    }
}
