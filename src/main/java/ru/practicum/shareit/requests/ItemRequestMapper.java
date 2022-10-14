package ru.practicum.shareit.requests;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestResponseDto;
import ru.practicum.shareit.requests.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requestor(null)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest, List<ItemDto> items) {
        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }


}