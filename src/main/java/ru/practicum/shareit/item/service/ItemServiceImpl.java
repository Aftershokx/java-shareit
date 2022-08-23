package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item create (long userId, Item item) {
        User owner = userRepository.getWithId (userId).orElseThrow (() ->
                new NoSuchElementException ("User not found"));
        item.setOwner (owner);
        return itemRepository.create (item);
    }

    @Override
    public Item update (long userId, long itemId, Item item) {
        Item updatedItem = getValidItemDto (userId, itemId, item);
        return itemRepository.update (itemId, updatedItem);
    }

    @Override
    public Item getWithId (long id) {
        return itemRepository.getWithId (id).orElseThrow (() ->
                new NoSuchElementException ("Item with id " + id + " not found"));
    }

    @Override
    public List<Item> searchWithText (String text) {
        if (text != null && !text.isBlank ())
            return itemRepository.searchWithText (text.toLowerCase (Locale.ROOT));
        return new ArrayList<> ();
    }

    @Override
    public List<Item> getAllWithUser (long userId) {
        return itemRepository.getAllWithUser (userId);
    }

    private Item getValidItemDto (long userId, long itemId, Item item) {
        Item updatedItem = itemRepository.getWithId (itemId).orElseThrow (
                () -> new NoSuchElementException ("Item with id " + itemId + " not found"));
        if (userRepository.getWithId (userId).isPresent () && updatedItem.getOwner ().getId () != userId)
            throw new NoSuchElementException ("Only owner can moderate items");
        if (item.getName () != null && !item.getName ().isBlank ())
            updatedItem.setName (item.getName ());
        if (item.getDescription () != null && !item.getDescription ().isBlank ()) {
            updatedItem.setDescription (item.getDescription ());
        }
        if (item.getAvailable () != null) {
            updatedItem.setAvailable (item.getAvailable ());
        }
        return updatedItem;
    }
}
