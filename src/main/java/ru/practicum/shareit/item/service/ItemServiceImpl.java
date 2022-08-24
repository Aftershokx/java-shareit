package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
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
    public Item create (long userId, ItemDto itemDto) {
        User owner = userRepository.getById (userId).orElseThrow (() ->
                new NoSuchElementException ("User not found"));
        Item item = ItemMapper.toItem (owner, itemDto);
        item.setOwner (owner);
        return itemRepository.create (item);
    }

    @Override
    public Item update (long userId, long itemId, ItemDto itemDto) {
        User user = userRepository.getById (userId).orElseThrow (() ->
                new NoSuchElementException ("User not found"));
        Item item = ItemMapper.toItem (user, itemDto);
        return itemRepository.update (itemId, getValidItemDto (userId, itemId, item));
    }

    @Override
    public Item getById (long id) {
        return itemRepository.getById (id).orElseThrow (() ->
                new NoSuchElementException ("Item By id " + id + " not found"));
    }

    @Override
    public List<Item> searchByText (String text) {
        if (text != null && !text.isBlank ())
            return itemRepository.searchByText (text.toLowerCase (Locale.ROOT));
        return new ArrayList<> ();
    }

    @Override
    public List<Item> getAllByUser (long userId) {
        return itemRepository.getAllByUser (userId);
    }

    private Item getValidItemDto (long userId, long itemId, Item item) {
        Item updatedItem = itemRepository.getById (itemId).orElseThrow (
                () -> new NoSuchElementException ("Item By id " + itemId + " not found"));
        if (userRepository.getById (userId).isPresent () && updatedItem.getOwner ().getId () != userId)
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
