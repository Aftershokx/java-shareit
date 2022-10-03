package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<List<Booking>> findAllByBooker_IdOrderByStartDesc(long bookerId);

    Optional<List<Booking>> findAllByBooker_IdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    Optional<List<Booking>> findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId,
                                                                                        LocalDateTime start,
                                                                                        LocalDateTime end);

    Optional<List<Booking>> findAllByBooker_IdAndStartIsAfterOrderByStartDesc(long bookerId, LocalDateTime start);

    Optional<List<Booking>> findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(long bookerId, LocalDateTime end);

    List<Booking> findAllByItem_IdOrderByStartDesc(long itemId);


    List<Booking> findAllByItem_IdAndStatusOrderByStartDesc(long itemId, BookingStatus status);

    List<Booking> findAllByItem_IdAndStartBeforeAndEndAfterOrderByStartDesc(long itemId,
                                                                            LocalDateTime start,
                                                                            LocalDateTime end);

    List<Booking> findAllByItem_IdAndStartIsAfterOrderByStartDesc(long itemId, LocalDateTime start);

    List<Booking> findAllByItem_IdAndEndIsBeforeOrderByStartDesc(long itemId, LocalDateTime end);

    boolean existsBookingByBooker_IdAndItem_IdAndStatusEqualsAndEndIsBefore(long bookerId, long itemId,
                                                                            BookingStatus status, LocalDateTime end);

    Optional<Booking> findFirstBookingByItem_IdAndEndIsBeforeOrderByEndDesc(long itemId, LocalDateTime now);

    Optional<Booking> findFirstBookingByItem_IdAndStartIsAfterOrderByStart(long itemId, LocalDateTime now);

}
