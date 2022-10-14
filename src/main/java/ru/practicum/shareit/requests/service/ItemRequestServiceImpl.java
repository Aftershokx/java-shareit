package ru.practicum.shareit.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.ItemRequestMapper;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestResponseDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto save(ItemRequestDto itemRequestDto, long userId) {
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Incorrect userId")));
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestResponseDto> findAll(long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("Пользователя с Id = " + userId + " нет в БД"));
        List<ItemRequestResponseDto> response = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId)) {
            response.add(ItemRequestMapper
                    .toItemRequestResponseDto(itemRequest, findByItemRequestId(itemRequest.getId())));
        }
        return response;
    }

    @Override
    public ItemRequestResponseDto findById(long userId, long itemRequestId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("Пользователя с Id = " + userId + " нет в БД"));
        ItemRequest itemRequest = itemRequestRepository
                .findById(itemRequestId).orElseThrow(() ->
                        new NoSuchElementException("Запроса с Id = " + itemRequestId + " нет в БД"));
        return ItemRequestMapper.toItemRequestResponseDto(itemRequest, findByItemRequestId(itemRequestId));
    }

    @Override
    public List<ItemRequestResponseDto> findAllWithPageable(long userId, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("created"));
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("Пользователя с Id = " + userId + " нет в БД"));

        List<ItemRequestResponseDto> response = new ArrayList<>();

        List<ItemRequest> itemRequests = itemRequestRepository.findAll(pageable)
                .stream()
                .filter(itemRequest -> itemRequest.getRequestor().getId() != userId)
                .collect(Collectors.toList());

        for (ItemRequest itemRequest : itemRequests) {
            response.add(ItemRequestMapper
                    .toItemRequestResponseDto(itemRequest, findByItemRequestId(itemRequest.getId())));
        }

        return response;
    }

    private List<ItemDto> findByItemRequestId(long id) {
        return itemRepository.findAllByItemRequestId(id)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}