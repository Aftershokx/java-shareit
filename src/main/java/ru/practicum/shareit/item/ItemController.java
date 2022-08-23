package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;
    private final UserService userService;

    @PostMapping
    public ItemDto create (@RequestHeader(USER_HEADER) long userId, @Valid @RequestBody ItemDto itemDto) {
        User user = userService.getWithId (userId);
        Item item = ItemMapper.toItem (user, itemDto);
        Item itemSaved = itemService.create (userId, item);
        return ItemMapper.toItemDto (itemSaved);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update (@RequestHeader(USER_HEADER) long userId, @PathVariable long itemId,
                           @RequestBody ItemDto itemDto) {
        User user = userService.getWithId (userId);
        Item item = ItemMapper.toItem (user, itemDto);
        Item itemUpdated = itemService.update (userId, itemId, item);
        return ItemMapper.toItemDto (itemUpdated);
    }

    @GetMapping("/{id}")
    public ItemDto getWithId (@PathVariable long id) {
        return ItemMapper.toItemDto (itemService.getWithId (id));
    }

    @GetMapping
    public List<ItemDto> getAllByUser (@RequestHeader(USER_HEADER) long userId) {
        return itemService.getAllWithUser (userId).stream ()
                .map (ItemMapper::toItemDto)
                .collect (Collectors.toList ());
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText (@RequestParam String text) {
        return itemService.searchWithText (text).stream ()
                .map (ItemMapper::toItemDto)
                .collect (Collectors.toList ());
    }
}