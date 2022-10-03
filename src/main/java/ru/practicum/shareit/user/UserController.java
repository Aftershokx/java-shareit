package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        return UserMapper.toUserDto(userService.getById(id));
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return UserMapper.toUserDto(
                userService.create(userDto)
        );
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable long id, @RequestBody User user) {
        return UserMapper.toUserDto(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable long id) {
        userService.remove(id);
    }
}

