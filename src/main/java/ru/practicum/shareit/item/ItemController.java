package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto create (@RequestHeader(USER_HEADER) long userId, @Valid @RequestBody ItemDto itemDto) {
        return ItemMapper.toItemDto (itemService.create (userId, itemDto));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update (@RequestHeader(USER_HEADER) long userId, @PathVariable long itemId,
                           @RequestBody ItemDto itemDto) {
        return ItemMapper.toItemDto (itemService.update (userId, itemId, itemDto));
    }

    @GetMapping("/{id}")
    public ItemDto getById (@PathVariable long id) {
        return ItemMapper.toItemDto (itemService.getById (id));
    }

    @GetMapping
    public List<ItemDto> getAllByUser (@RequestHeader(USER_HEADER) long userId) {
        return itemService.getAllByUser (userId).stream ()
                .map (ItemMapper::toItemDto)
                .collect (Collectors.toList ());
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText (@RequestParam String text) {
        return itemService.searchByText (text).stream ()
                .map (ItemMapper::toItemDto)
                .collect (Collectors.toList ());
    }
}