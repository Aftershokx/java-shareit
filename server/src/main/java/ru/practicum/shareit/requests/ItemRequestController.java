package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestResponseDto;
import ru.practicum.shareit.requests.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto add(@RequestHeader(USER_HEADER) long userId,
                              @RequestBody ItemRequestDto itemRequestDto) {
        return service.save(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestResponseDto> findAll(@RequestHeader(USER_HEADER) long userId) {
        return service.findAll(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> findAllWithPageable(@RequestHeader(USER_HEADER) long userId,
                                                            @RequestParam(defaultValue = "0") int from,
                                                            @RequestParam(defaultValue = "20") int size) {
        return service.findAllWithPageable(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto findByRequestId(@RequestHeader(USER_HEADER) long userId,
                                                  @PathVariable long requestId) {
        return service.findById(userId, requestId);
    }
}