package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class ItemMapper {
    public static ItemDto toItemDto (Item item) {
        ItemDto.ItemRequest itemRequest = new ItemDto.ItemRequest ();
        if (item.getRequest () != null)
            itemRequest.setId (item.getRequest ().getId ());
        return ItemDto.builder ()
                .id (item.getId ())
                .name (item.getName ())
                .description (item.getDescription ())
                .available (item.getAvailable ())
                .request (itemRequest)
                .build ();
    }

    public static Item toItem (User user, ItemDto itemDto) {
        return Item.builder ()
                .id (itemDto.getId ())
                .name (itemDto.getName ())
                .description (itemDto.getDescription ())
                .available (itemDto.getAvailable ())
                .owner (user)
                .build ();
    }
}
