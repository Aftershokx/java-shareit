package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForBooking;
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
    public ItemDto create(@RequestHeader(USER_HEADER) long userId, @Valid @RequestBody ItemDto itemDto){
        return ItemMapper.toItemDto(itemService.create(userId, itemDto));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_HEADER) long userId, @PathVariable long itemId,
                          @RequestBody ItemDto itemDto){
        return ItemMapper.toItemDto(itemService.update(userId, itemId, itemDto));
    }

    @GetMapping("/{id}")
    public ItemDtoForBooking getById(@RequestHeader(USER_HEADER) long userId, @PathVariable long id){
        return ItemMapper.toItemDtoWithBooking(itemService.getById(id, userId));
    }

    @GetMapping
    public List<ItemDtoForBooking> getAllByUser(@RequestHeader(USER_HEADER) long userId){
        return itemService.getAllByUser(userId).stream()
                .map(ItemMapper::toItemDtoWithBooking)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestParam String text){
        return itemService.searchByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader(USER_HEADER) long userId, @PathVariable long itemId){
        itemService.delete(userId, itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_HEADER) long userId, @PathVariable long itemId,
                                 @Valid @RequestBody CommentDto commentDto){
        return CommentMapper.toCommentDto(itemService.addComment(userId, itemId, commentDto));
    }
}