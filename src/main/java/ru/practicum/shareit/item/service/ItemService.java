package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create (long userId, Item item);

    Item update (long userId, long itemId, Item item);

    Item getWithId (long id);

    List<Item> getAllWithUser (long userId);

    List<Item> searchWithText (String text);
}
