package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    Item update(long userId, long itemId, ItemDto itemDto);

    Item getById(long id, long userId);

    List<Item> searchByText(String text, int from, int size);

    void delete(long userId, long itemId);

    Comment addComment(long userId, long itemId, CommentDto commentDto);

    List<Item> findAll(long userId, int from, int size);

}
