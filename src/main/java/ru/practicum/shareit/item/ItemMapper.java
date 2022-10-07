package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        Long id = null;
        ItemRequest request = item.getItemRequest();
        if (request != null) {
            id = (request.getId());
        }
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(id)
                .comments(item.getComments() != null && item.getComments().size() != 0 ? item.getComments().stream()
                        .map(CommentMapper::toCommentDto).collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }

    public static ItemDtoForBooking toItemDtoWithBooking(Item item) {
        return ItemDtoForBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(item.getComments() != null && item.getComments().size() != 0 ? item.getComments().stream()
                        .map(CommentMapper::toCommentDto).collect(Collectors.toList()) : new ArrayList<>())
                .lastBooking(item.getLastBooking() != null ? ItemDtoForBooking.BookingDto.builder()
                        .id(item.getLastBooking().getId())
                        .bookerId(item.getLastBooking().getBooker().getId())
                        .start(item.getLastBooking().getStart())
                        .end(item.getLastBooking().getEnd())
                        .build() : null)
                .nextBooking(item.getNextBooking() != null ? ItemDtoForBooking.BookingDto.builder()
                        .id(item.getNextBooking().getId())
                        .bookerId(item.getNextBooking().getBooker().getId())
                        .start(item.getNextBooking().getStart())
                        .end(item.getNextBooking().getEnd())
                        .build() : null)
                .build();
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
