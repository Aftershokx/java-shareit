package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final CommentRepository commentsRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("User not found"));
        Item item = ItemMapper.toItem(owner, itemDto);
        item.setOwner(owner);
        Long requestId = itemDto.getRequestId();
        if (requestId != null) {
            item.setItemRequest(itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NoSuchElementException("Incorrect RequestId")));
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public Item update(long userId, long itemId, ItemDto itemDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NoSuchElementException("User not found"));
        Item item = ItemMapper.toItem(user, itemDto);
        checkOwner(userId, itemId);
        return itemRepository.save(getValidItemDto(userId, itemId, item));
    }

    @Override
    public Item getById(long id, long userId) {
        Item item = itemRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Item By id " + id + " not found"));
        itemSetCommentsAndBookings(item);
        if (item.getOwner().getId() != userId) {
            item.setLastBooking(null);
            item.setNextBooking(null);
        }
        return item;
    }

    @Override
    public List<Item> searchByText(String text, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        if (text != null && !text.isBlank())
            return itemRepository.searchByText(text.toLowerCase(Locale.ROOT), pageable);
        return new ArrayList<>();
    }

    @Override
    public List<Item> findAll(long userId, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);
        List<Item> items = new ArrayList<>(itemRepository.findByOwnerId(userId, pageable));
        items.forEach(this::itemSetCommentsAndBookings);
        return items.stream().sorted(Comparator.comparing(Item::getId)).collect(Collectors.toList());
    }

    @Override
    public void delete(long userId, long itemId) {
        checkOwner(userId, itemId);
        itemRepository.deleteById(itemId);
    }

    @Override
    public Comment addComment(long userId, long itemId, CommentDto commentDto) {
        if (bookingService.checkBooking(userId, itemId, BookingStatus.APPROVED)) {
            return commentsRepository.save(CommentMapper.toComment(commentDto, userRepository.findById(userId).orElseThrow(() ->
                            new NoSuchElementException("User not found")),
                    getById(itemId, userId)));
        } else {
            throw new ItemNotAvailableException("User " + userId + " has no booking for " + itemId + " item");
        }
    }


    private void checkOwner(Long userId, Long itemId) {
        Item item = getById(itemId, userId);
        if (item.getOwner().getId() != userId) {
            throw new NoSuchElementException("User does not own this item");
        }
    }

    private Item getValidItemDto(long userId, long itemId, Item item) {
        Item updatedItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NoSuchElementException("Item By id " + itemId + " not found"));
        if (userRepository.findById(userId).isPresent() && updatedItem.getOwner().getId() != userId)
            throw new NoSuchElementException("Only owner can moderate items");
        if (item.getName() != null && !item.getName().isBlank())
            updatedItem.setName(item.getName());
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            updatedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }
        return updatedItem;
    }

    private void itemSetCommentsAndBookings(Item item) {
        if (!commentsRepository.findAllByItem_Id(item.getId()).isEmpty()) {
            item.setComments(new ArrayList<>(commentsRepository.findAllByItem_Id(item.getId())));
        }
        if (bookingService.getLastBooking(item.getId()).isPresent()) {
            item.setLastBooking(bookingService.getLastBooking(item.getId()).get());
        }
        if (bookingService.getNextBooking(item.getId()).isPresent()) {
            item.setNextBooking(bookingService.getNextBooking(item.getId()).get());
        }
    }

}
