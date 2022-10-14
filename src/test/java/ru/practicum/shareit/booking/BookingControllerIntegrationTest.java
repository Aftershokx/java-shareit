package ru.practicum.shareit.booking;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingControllerIntegrationTest {
    private final EntityManager entityManager;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
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
    private final Item anotherItem = Item.builder()
            .id(2L)
            .name("ItemName2")
            .description("ItemDesc2")
            .owner(owner)
            .available(true)
            .build();

    @Test
    public void addBooking() {
        User ownerCreated = userService.create(UserMapper.toUserDto(owner));
        User bookerCreated = userService.create(UserMapper.toUserDto(booker));
        ItemDto createdItem = itemService.create(ownerCreated.getId(), ItemMapper.toItemDto(item));
        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .startDate(date.plusDays(1))
                .endDate(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        Booking createdBooking = bookingService.add(bookerCreated.getId(), bookingRequestDto);
        TypedQuery<Booking> query = entityManager.createQuery(
                "select b from Booking b where b.id = : id", Booking.class);
        Booking booking1 = query.setParameter("id", createdBooking.getId())
                .getSingleResult();

        assertThat(booking1.getId(), notNullValue());
        assertThat(booking1.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(booking1.getItem().getId(), equalTo(createdItem.getId()));
    }

    @Test
    public void approveBooking() {
        User ownerCreated = userService.create(UserMapper.toUserDto(owner));
        User bookerCreated = userService.create(UserMapper.toUserDto(booker));
        ItemDto createdItem = itemService.create(ownerCreated.getId(), ItemMapper.toItemDto(item));
        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .startDate(date.plusDays(1))
                .endDate(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        Booking createdBooking = bookingService.add(bookerCreated.getId(), bookingRequestDto);
        Booking approveBooking = bookingService.bookingConfirmation(ownerCreated.getId(), createdBooking.getId(), true);

        TypedQuery<Booking> query = entityManager.createQuery(
                "select b from Booking b where b.id = : id", Booking.class);
        Booking booking1 = query.setParameter("id", approveBooking.getId())
                .getSingleResult();

        assertThat(booking1.getId(), notNullValue());
        assertThat(booking1.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(booking1.getItem().getId(), equalTo(createdItem.getId()));
    }

    @Test
    public void getAllByBookerId() {
        User ownerCreated = userService.create(UserMapper.toUserDto(owner));
        User bookerCreated = userService.create(UserMapper.toUserDto(booker));
        ItemDto createdItem = itemService.create(ownerCreated.getId(), ItemMapper.toItemDto(item));
        ItemDto createdAnotherItem = itemService.create(ownerCreated.getId(), ItemMapper.toItemDto(anotherItem));
        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .startDate(date.plusDays(1))
                .endDate(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        Booking createdBooking = bookingService.add(bookerCreated.getId(), bookingRequestDto);
        BookingRequestDto anotherBookingRequestDto = BookingRequestDto.builder()
                .startDate(date.plusDays(1))
                .endDate(date.plusDays(2))
                .itemId(createdAnotherItem.getId())
                .build();
        Booking createdBooking2 = bookingService.add(bookerCreated.getId(), anotherBookingRequestDto);
        List<Booking> bookings = bookingService.getAll(bookerCreated.getId(), "ALL", 0, 2);

        assertThat(bookings.size(), equalTo(2));
        assertTrue(bookings.contains(createdBooking));
        assertTrue(bookings.contains(createdBooking2));
    }

    @Test
    public void getAllByOwnerId() {
        User ownerCreated = userService.create(UserMapper.toUserDto(owner));
        User bookerCreated = userService.create(UserMapper.toUserDto(booker));
        ItemDto createdItem = itemService.create(ownerCreated.getId(), ItemMapper.toItemDto(item));
        ItemDto createdAnotherItem = itemService.create(ownerCreated.getId(), ItemMapper.toItemDto(anotherItem));
        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .startDate(date.plusDays(1))
                .endDate(date.plusDays(2))
                .itemId(createdItem.getId())
                .build();
        Booking createdBooking = bookingService.add(bookerCreated.getId(), bookingRequestDto);
        BookingRequestDto anotherBookingRequestDto = BookingRequestDto.builder()
                .startDate(date.plusDays(1))
                .endDate(date.plusDays(2))
                .itemId(createdAnotherItem.getId())
                .build();
        Booking createdBooking2 = bookingService.add(bookerCreated.getId(), anotherBookingRequestDto);
        List<Booking> bookings = bookingService.getAllBookingByOwner(ownerCreated.getId(), "ALL", 0, 2);

        assertThat(bookings.size(), equalTo(2));
        assertTrue(bookings.contains(createdBooking));
        assertTrue(bookings.contains(createdBooking2));
    }
}