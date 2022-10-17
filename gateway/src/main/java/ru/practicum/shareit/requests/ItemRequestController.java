package ru.practicum.shareit.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    public static final String USER_HEADER = "X-Sharer-User-Id";
    private final ItemRequestClient requestClient;

    @Autowired
    public ItemRequestController(ItemRequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_HEADER) long userId,
                                      @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return requestClient.save(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader(USER_HEADER) long userId) {
        return requestClient.getByRequestorId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllWithPageable(@RequestHeader(USER_HEADER) long userId,
                                                      @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                      @RequestParam(defaultValue = "20") @Positive int size) {
        return requestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByRequestId(@RequestHeader(USER_HEADER) long userId,
                                                  @PathVariable long requestId) {
        return requestClient.getById(userId, requestId);
    }
}