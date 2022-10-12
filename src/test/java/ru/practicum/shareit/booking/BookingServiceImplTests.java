package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTests {
    private final LocalDateTime date = LocalDateTime.now();
    private final User owner = User.builder()
            .id(1L)
            .name("UserName")
            .email("user@mail.ru").build();
    private final User booker = User.builder()
            .id(2L)
            .name("BookerName")
            .email("booker@mail.ru").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("ItemDesc")
            .owner(owner)
            .available(true)
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(date.minusDays(1))
            .end(date.minusHours(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.APPROVED)
            .build();
    private final BookingRequestDto bookingInputDto = BookingRequestDto.builder()
            .id(booking.getId())
            .startDate(booking.getStart())
            .endDate(booking.getEnd())
            .itemId(booking.getItem().getId())
            .build();
    private final Booking notApproveBooking = Booking.builder()
            .id(2L)
            .start(date.minusDays(1))
            .end(date.minusHours(1))
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();
    private final Item anotherItem = Item.builder()
            .id(2L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(false)
            .build();
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;

    @Test
    void addBooking() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking bookingCreated = bookingService.add(2L, bookingInputDto);
        assertNotEquals(bookingCreated, null);
        assertEquals(booking.getId(), bookingCreated.getId());
        assertEquals(booking.getItem().getId(), bookingCreated.getItem().getId());
        assertEquals(booking.getStart(), bookingCreated.getStart());
        assertEquals(booking.getEnd(), bookingCreated.getEnd());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void addBookingWhenItemUnknownShouldThrowException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.add(2L, bookingInputDto));
        assertEquals("Item By id not found", exception.getMessage());
    }

    @Test
    void addBookingWhenBookerUnknownShouldThrowException() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.add(2L, bookingInputDto));
        assertEquals("Item By id not found", exception.getMessage());
    }

    @Test
    void addBookingWhenBookerIsOwnerShouldThrowException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.add(1L, bookingInputDto));
        assertEquals("user 1 cannot book his own item 1", exception.getMessage());
    }

    @Test
    void addBookingWhenItemNotAvailableShouldThrowException() {
        Item anotherItemTwo = Item.builder()
                .id(2L)
                .name("ItemName2")
                .description("ItemDesc2")
                .owner(booker)
                .available(false)
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(anotherItemTwo));

        ItemNotAvailableException exception = assertThrows(ItemNotAvailableException.class,
                () -> bookingService.add(1L, bookingInputDto));
        assertEquals("Item 2 unreliable", exception.getMessage());
    }

    @Test
    void addBookingWhenItemNotFoundShouldThrowException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.add(1L, bookingInputDto));
        assertEquals("Item By id not found", exception.getMessage());
    }

    @Test
    void addBookingWhenUserNotFoundShouldThrowException() {
        Item anotherItemTwo = Item.builder()
                .id(2L)
                .name("ItemName2")
                .description("ItemDesc2")
                .owner(booker)
                .available(true)
                .build();
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(anotherItemTwo));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.add(1L, bookingInputDto));
        assertEquals("UserNotFound By id not found", exception.getMessage());
    }

    @Test
    void approveBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(notApproveBooking));
        when(bookingRepository.save(any())).thenReturn(notApproveBooking);

        Booking bookingApproved = bookingService.bookingConfirmation(1L, 2L, true);
        assertEquals(notApproveBooking.getId(), bookingApproved.getId());
        assertEquals(notApproveBooking.getItem().getId(), bookingApproved.getItem().getId());
        assertEquals(notApproveBooking.getStart(), bookingApproved.getStart());
        assertEquals(notApproveBooking.getEnd(), bookingApproved.getEnd());
        assertEquals(BookingStatus.APPROVED, bookingApproved.getStatus());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void approveBookingWithApprovedStateShouldThrowException() {
        Booking bookingNew = Booking.builder()
                .id(1L)
                .start(date.minusDays(1))
                .end(date.minusHours(1))
                .item(item)
                .booker(owner)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingNew));

        ItemNotAvailableException exception = assertThrows(ItemNotAvailableException.class,
                () -> bookingService.bookingConfirmation(2L, 1L, true));
        assertEquals("Cant change approved bookings", exception.getMessage());

    }

    @Test
    void approveBookingByNonOwnerShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(notApproveBooking));

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.bookingConfirmation(2L, 2L, true));
        assertEquals("User does not own this item", exception.getMessage());
    }

    @Test
    void approveBookingWhenStatusIsNotWaitingShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        ItemNotAvailableException exception = assertThrows(ItemNotAvailableException.class,
                () -> bookingService.bookingConfirmation(1L, 1L, true));
        assertEquals("Cant change approved bookings", exception.getMessage());
    }

    @Test
    void rejectBookingTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(notApproveBooking));
        when(bookingRepository.save(any())).thenReturn(notApproveBooking);

        Booking bookingApproved = bookingService.bookingConfirmation(1L, 2L, false);
        assertEquals(notApproveBooking.getId(), bookingApproved.getId());
        assertEquals(notApproveBooking.getItem().getId(), bookingApproved.getItem().getId());
        assertEquals(notApproveBooking.getStart(), bookingApproved.getStart());
        assertEquals(notApproveBooking.getEnd(), bookingApproved.getEnd());
        assertEquals(BookingStatus.REJECTED, bookingApproved.getStatus());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void getBookingByIdByOwner() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Booking booking = bookingService.getById(1L, 1L);
        assertNotEquals(booking, null);
        assertEquals(booking.getItem().getId(), item.getId());
        assertEquals(booking.getBooker().getId(), booker.getId());

        verify(bookingRepository, times(1)).findById(any());
    }

    @Test
    void getBookingByIdByBooker() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Booking booking = bookingService.getById(2L, 1L);
        assertNotEquals(booking, null);
        assertEquals(booking.getItem().getId(), item.getId());
        assertEquals(booking.getBooker().getId(), booker.getId());

        verify(bookingRepository, times(1)).findById(any());
    }

    @Test
    void getBookingByIdByNonOwnerOrNonBookerShouldThrowException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getById(3L, 1L));
        assertEquals("User does not own this item", exception.getMessage());
    }

    @Test
    void getAllByBooker() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdOrderByStartDesc(anyLong(), any()))
                .thenReturn(Optional.of(List.of(booking, notApproveBooking)));

        List<Booking> bookings = bookingService.getAll(2L, "ALL", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);
        assertEquals(bookings.get(1).getStatus(), BookingStatus.WAITING);

        verify(bookingRepository, times(1)).findAllByBooker_IdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByBookerNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getAll(2L, "ALL", 0, 2));

        assertEquals("User By id 2 not found", exception.getMessage());
    }

    @Test
    void getAllByBookerWithoutBookings() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdOrderByStartDesc(anyLong(), any()))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getAll(1L, "ALL", 0, 2));
        assertEquals("Bookings for user " + 1 + " not found", exception.getMessage());
    }

    @Test
    void getAllByBookerWithStateWaiting() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.of(List.of(notApproveBooking)));

        List<Booking> bookings = bookingService.getAll(2L, "WAITING", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.WAITING);

        verify(bookingRepository, times(1))
                .findAllByBooker_IdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByBookerWithStateWaitingNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getAll(2L, "WAITING", 0, 2));
        assertEquals("Waiting bookings for user  " +
                2 + " not found", exception.getMessage());
    }

    @Test
    void getAllByBookerWithStateRejected() {
        final Booking rejectedBooking = Booking.builder()
                .id(3L)
                .start(date.minusDays(1))
                .end(date.minusHours(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.REJECTED)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.of(List.of(rejectedBooking)));

        List<Booking> bookings = bookingService.getAll(2L, "REJECTED", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    void getAllByBookerWithStateRejectedNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getAll(2L, "REJECTED", 0, 2));
        assertEquals("Rejected bookings for user "
                + 2 + " not found", exception.getMessage());
    }

    @Test
    void getAllByBookerWithStatePast() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.of(List.of(booking, notApproveBooking)));

        List<Booking> bookings = bookingService.getAll(2L, "PAST", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertTrue(bookings.get(0).getEnd().isBefore(date));
        assertTrue(bookings.get(1).getEnd().isBefore(date));

        verify(bookingRepository, times(1)).findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByBookerWithStatePastNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getAll(2L, "PAST", 0, 2));
        assertEquals("Past bookings for user "
                + 2 + " not found", exception.getMessage());
    }

    @Test
    void getAllByBookerWithStateFuture() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.of(Collections.emptyList()));

        List<Booking> bookings = bookingService.getAll(2L, "FUTURE", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 0);
    }

    @Test
    void getAllByBookerWithStateFutureNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getAll(2L, "FUTURE", 0, 2));
        assertEquals("Future bookings for user "
                + 2 + " not found", exception.getMessage());
    }

    @Test
    void getAllByBookerWithStateCurrent() {
        final Booking currentBooking = Booking.builder()
                .id(3L)
                .start(date.minusDays(1))
                .end(date.plusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(Optional.of(List.of(currentBooking)));

        List<Booking> bookings = bookingService.getAll(2L, "CURRENT", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertTrue(bookings.get(0).getEnd().isAfter(date));
    }

    @Test
    void getAllByBookerWithStateCurrentNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> bookingService.getAll(2L, "CURRENT", 0, 2));
        assertEquals("Current bookings for user "
                + 2 + " not found", exception.getMessage());
    }

    @Test
    void getAllByBookerWithStateUnknownShouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));

        UnsupportedStatusException exception = assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getAll(2L, "OLD", 0, 2));
        assertEquals("Unknown state: OLD", exception.getMessage());
    }

    @Test
    void getAllByOwnerWithStateUnknownShouldThrowException() {
        when(itemRepository.findByOwner(owner)).thenReturn(List.of(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));

        UnsupportedStatusException exception = assertThrows(UnsupportedStatusException.class,
                () -> bookingService.getAllBookingByOwner(2L, "OLD", 0, 2));
        assertEquals("Unknown state: OLD", exception.getMessage());
    }

    @Test
    void getAllByOwnerId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(any()))
                .thenReturn(List.of(item, anotherItem));
        when(bookingRepository.findAllByItem_IdOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getAllBookingByOwner(1L, "ALL", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository, times(2)).findAllByItem_IdOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByOwnerIdWithStateCurrent() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(any()))
                .thenReturn(List.of(item, anotherItem));
        when(bookingRepository
                .findAllByItem_IdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getAllBookingByOwner(1L, "CURRENT", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository, times(2))
                .findAllByItem_IdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any(), any());
    }

    @Test
    void getAllByOwnerIdWithStateFuture() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(any()))
                .thenReturn(List.of(item, anotherItem));
        when(bookingRepository
                .findAllByItem_IdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getAllBookingByOwner(1L, "FUTURE", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository, times(2))
                .findAllByItem_IdAndStartIsAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByOwnerIdWithStatePast() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(any()))
                .thenReturn(List.of(item, anotherItem));
        when(bookingRepository
                .findAllByItem_IdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getAllBookingByOwner(1L, "PAST", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository, times(2))
                .findAllByItem_IdAndEndIsBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByOwnerIdWithStateWaiting() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(any()))
                .thenReturn(List.of(item, anotherItem));
        when(bookingRepository
                .findAllByItem_IdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getAllBookingByOwner(1L, "WAITING", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository, times(2))
                .findAllByItem_IdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByOwnerIdWithStateRejected() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwner(any()))
                .thenReturn(List.of(item, anotherItem));
        when(bookingRepository
                .findAllByItem_IdAndStatusOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getAllBookingByOwner(1L, "REJECTED", 0, 2);
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 2);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);

        verify(bookingRepository, times(2))
                .findAllByItem_IdAndStatusOrderByStartDesc(anyLong(), any(), any());
    }
}