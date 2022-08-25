package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> repository = new HashMap<> ();
    private long id = 0L;

    @Override
    public List<Item> getAll () {
        return new ArrayList<> (repository.values ());
    }

    @Override
    public Optional<Item> getById (long id) {
        return Optional.ofNullable (repository.get (id));
    }

    @Override
    public Item create (Item item) {
        item.setId (genId ());
        repository.put (id, item);
        log.info ("Created Item {}", item);
        return item;
    }

    @Override
    public List<Item> searchByText (String text) {
        return getAll ().stream ()
                .filter (Item::getAvailable)
                .filter (item -> item.getName ().toLowerCase (Locale.ROOT).contains (text) ||
                        item.getDescription ().toLowerCase (Locale.ROOT).contains (text))
                .collect (Collectors.toList ());
    }

    @Override
    public List<Item> getAllByUser (long userId) {
        return getAll ().stream ()
                .filter (itemDto -> itemDto.getOwner ().getId () == userId)
                .collect (Collectors.toList ());
    }

    @Override
    public void remove (long id) {
        repository.remove (id);
    }

    @Override
    public Item update (long id, Item item) {
        repository.put (id, item);
        log.info ("Updated Item {}", item);
        return item;
    }

    private Long genId () {
        return ++id;
    }
}