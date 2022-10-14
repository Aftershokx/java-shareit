package ru.practicum.shareit.requests.service;

import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto save(ItemRequestDto itemRequestDto, long userId);

    List<ItemRequestResponseDto> findAll(long userId);

    ItemRequestResponseDto findById(long userId, long itemRequestId);

    List<ItemRequestResponseDto> findAllWithPageable(long userId, int from, int size);

}
