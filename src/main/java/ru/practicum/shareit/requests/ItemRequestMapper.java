package ru.practicum.shareit.requests;

import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestDto.User requestor = new ItemRequestDto.User();
        if (itemRequest.getRequestor() != null)
            requestor.setId(itemRequest.getRequestor().getId());
        return ItemRequestDto.builder()
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .requestor(requestor)
                .build();
    }
}