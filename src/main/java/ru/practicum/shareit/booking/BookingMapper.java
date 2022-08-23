package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {
    public static BookingDto toBookingDto (Booking booking) {
        BookingDto.Item item = new BookingDto.Item ();
        BookingDto.User booker = new BookingDto.User ();
        if (booking.getItem () != null)
            item.setId (booking.getItem ().getId ());
        if (booking.getBooker () != null)
            booker.setId (booking.getBooker ().getId ());
        return BookingDto.builder ()
                .start (booking.getStart ())
                .end (booking.getEnd ())
                .item (item)
                .booker (booker)
                .status (booking.getStatus ())
                .build ();
    }
}