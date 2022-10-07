package ru.practicum.shareit.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.requests.ItemRequestMapper;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper mapper;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto save(ItemRequestDto itemRequestDto, long userId) {
        ItemRequest itemRequest = mapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Incorrect userId")));
        return mapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDtoWithItems> findAll(long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("Пользователя с Id = " + userId + " нет в БД"));
        return itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(mapper::toItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoWithItems findById(long userId, long itemRequestId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("Пользователя с Id = " + userId + " нет в БД"));
        ItemRequest itemRequest = itemRequestRepository
                .findById(itemRequestId).orElseThrow(() ->
                        new NoSuchElementException("Запроса с Id = " + itemRequestId + " нет в БД"));
        return mapper.toItemRequestDtoWithItems(itemRequest);
    }

    @Override
    public List<ItemRequestDtoWithItems> findAllWithPageable(long userId, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("created"));
        userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("Пользователя с Id = " + userId + " нет в БД"));
        return itemRequestRepository.findAll(pageable)
                .stream()
                .filter(itemRequest -> itemRequest.getRequestor().getId() != userId)
                .map(mapper::toItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

}