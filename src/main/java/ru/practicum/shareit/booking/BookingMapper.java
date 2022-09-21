package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingResponseDto toBookingDto (Booking booking) {
        return BookingResponseDto.builder ()
                .id (booking.getId ())
                .booker (UserMapper.toUserDto (booking.getBooker ()))
                .item (ItemMapper.toItemDto (booking.getItem ()))
                .startDate (booking.getStart ())
                .endDate (booking.getEnd ())
                .status (booking.getStatus ())
                .build ();
    }

    public static Booking toBooking (BookingRequestDto bookingRequestDto, User booker, Item item) {
        return Booking.builder ()
                .id (bookingRequestDto.getId ())
                .booker (booker)
                .item (item)
                .start (bookingRequestDto.getStartDate ())
                .end (bookingRequestDto.getEndDate ())
                .status (bookingRequestDto.getStatus ())
                .build ();
    }
}