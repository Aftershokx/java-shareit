package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<List<Booking>> findAllByBooker_IdOrderByStartDesc(long bookerId, Pageable pageable);

    Optional<List<Booking>> findAllByBooker_IdAndStatusOrderByStartDesc(long bookerId, BookingStatus status, Pageable pageable);

    Optional<List<Booking>> findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId,
                                                                                        LocalDateTime start,
                                                                                        LocalDateTime end, Pageable pageable);

    Optional<List<Booking>> findAllByBooker_IdAndStartIsAfterOrderByStartDesc(long bookerId, LocalDateTime start, Pageable pageable);

    Optional<List<Booking>> findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByItem_IdOrderByStartDesc(long itemId, Pageable pageable);


    List<Booking> findAllByItem_IdAndStatusOrderByStartDesc(long itemId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByItem_IdAndStartBeforeAndEndAfterOrderByStartDesc(long itemId,
                                                                            LocalDateTime start,
                                                                            LocalDateTime end, Pageable pageable);

    List<Booking> findAllByItem_IdAndStartIsAfterOrderByStartDesc(long itemId, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByItem_IdAndEndIsBeforeOrderByStartDesc(long itemId, LocalDateTime end, Pageable pageable);

    boolean existsBookingByBooker_IdAndItem_IdAndStatusEqualsAndEndIsBefore(long bookerId, long itemId,
                                                                            BookingStatus status, LocalDateTime end);

    Optional<Booking> findFirstBookingByItem_IdAndEndIsBeforeOrderByEndDesc(long itemId, LocalDateTime now);

    Optional<Booking> findFirstBookingByItem_IdAndStartIsAfterOrderByStart(long itemId, LocalDateTime now);

}
