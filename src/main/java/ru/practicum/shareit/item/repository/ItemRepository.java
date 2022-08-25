package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> getAllByUser (long userId);

    List<Item> searchByText (String text);

    List<Item> getAll ();

    Optional<Item> getById (long id);

    Item create (Item item);

    void remove (long id);

    Item update (long id, Item item);
}
