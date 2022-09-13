package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDto;
import ru.practicum.shareit.booking.dto.BookingOutputDtoForItem;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingOutputDto toBookingDto(Booking booking) {
        return BookingOutputDto.builder()
                .id(booking.getId())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDto(booking.getItem()))
                .startDate(booking.getStart())
                .endDate(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public static BookingOutputDtoForItem toBookingDtoForItem(Booking booking) {
        return BookingOutputDtoForItem.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .item(ItemMapper.toItemDto(booking.getItem()))
                .startDate(booking.getStart())
                .endDate(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingInputDto bookingInputDto, User booker, Item item) {
        return Booking.builder()
                .id(bookingInputDto.getId())
                .booker(booker)
                .item(item)
                .start(bookingInputDto.getStartDate())
                .end(bookingInputDto.getEndDate())
                .status(bookingInputDto.getStatus())
                .build();
    }
}