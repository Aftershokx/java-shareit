package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestDtoWithItems;
import ru.practicum.shareit.requests.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/requests")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestController {

    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto add(@RequestHeader(USER_HEADER) long userId,
                              @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return service.save(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDtoWithItems> findAll(@RequestHeader(USER_HEADER) long userId) {
        return service.findAll(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoWithItems> findByRequestId(@RequestHeader(USER_HEADER) long userId,
                                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                                         @RequestParam(defaultValue = "20") @Positive int size) {
        return service.findAllWithPageable(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoWithItems findByRequestId(@RequestHeader(USER_HEADER) long userId,
                                                   @PathVariable long requestId) {
        return service.findById(userId, requestId);
    }
}