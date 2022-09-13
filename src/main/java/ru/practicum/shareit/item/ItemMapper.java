package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto.ItemRequest itemRequest = new ItemDto.ItemRequest();
        if (item.getRequest() != null)
            itemRequest.setId(item.getRequest().getId());
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(itemRequest)
                .comments(item.getComments() != null && item.getComments().size() != 0 ? item.getComments().stream()
                        .map(CommentMapper::toCommentDto).collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    public static ItemDtoForBooking toItemDtoWithBooking(Item item) {
        return ItemDtoForBooking.builder().
                id(item.getId()).
                name(item.getName()).
                description(item.getDescription()).
                available(item.getAvailable()).
                comments(item.getComments() != null && item.getComments().size() != 0 ? item.getComments().stream()
                        .map(CommentMapper::toCommentDto).collect(Collectors.toList()) : new ArrayList<>()).
                lastBooking(item.getLastBooking() != null ? BookingMapper.toBookingDtoForItem(item.getLastBooking()) : null).
                nextBooking(item.getNextBooking() != null ? BookingMapper.toBookingDtoForItem(item.getNextBooking()) : null).
                build();
    }

    public static Item toItem(User user, ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .build();
    }
}
